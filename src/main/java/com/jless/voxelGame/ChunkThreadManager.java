package com.jless.voxelGame;

import java.util.*;
import java.util.concurrent.*;

import org.joml.*;

import com.jless.voxelGame.chunkGen.*;
import com.jless.voxelGame.worldGen.*;

public class ChunkThreadManager {

  private final World world;
  private static ExecutorService executor;
  private final ConcurrentLinkedQueue<Chunk> uploadQueue;
  private final Set<Long> rebuildingChunks = ConcurrentHashMap.newKeySet();
  private final Queue<Vector2i> generationQueue = new ConcurrentLinkedQueue<>();
  private final ConcurrentHashMap<Long, CompletableFuture<Chunk>> generatingChunks;

  private static final int MAX_CHUNK_PER_FRAME = 2;
  private int recoveryCount = 0;

  ChunkThreadManager(World world) {
    this.world = world;
    ChunkThreadManager.executor = Executors.newFixedThreadPool(Consts.THREAD_COUNT);
    this.uploadQueue = new ConcurrentLinkedQueue<>();
    this.generatingChunks = new ConcurrentHashMap<>();
  }

  public void queueForUpload(Chunk chunk) {
    uploadQueue.offer(chunk);
  }

  public synchronized void processUploads() {
    int processed = 0;
    int rebuildCount = 0;
    int rebuildLimit = 10;
    int uploadLimit = Consts.MAX_CHUNK_UPLOADS_PER_FRAME;
    int startedThisFrame = 0;

    while(startedThisFrame < MAX_CHUNK_PER_FRAME && !generationQueue.isEmpty()) {
      Vector2i pos = generationQueue.poll();
      if(pos == null) break;

      generateChunkAsync(pos.x, pos.y);
      startedThisFrame++;
    }

    while(!uploadQueue.isEmpty() && processed < uploadLimit) {
      Chunk upload = uploadQueue.poll();
      if(upload == null) break;

      if(upload.hasPendingMesh()) {
        boolean success = upload.uploadPendingMesh();
        if(success && upload.uploaded) {
          markNeighborDirty(upload);
          world.markChunkGenerated(upload.pos.x, upload.pos.z);
          processed++;
        } else {
          System.err.println("Err: Upload failed");
          upload.markDirty();
        }
      } else {
        upload.markDirty();
        continue;
      }
    }
    recoverStuckChunks();

    for(Chunk chunk : world.chunks.values()) {
      if(rebuildCount >= rebuildLimit) break;
      if(chunk.dirty && chunk.hasAllNeighbors(world)) {
        long key = World.chunkKey(chunk.pos.x, chunk.pos.z);

        if(rebuildingChunks.add(key)) {
          final int cx = chunk.pos.x;
          final int cz = chunk.pos.z;
          executor.submit(() -> {
            try {
              Chunk c = world.getChunkIfLoaded(cx, cz);
              if(c != null && c.dirty) {
                c.buildMeshAsync(world);
                queueForUpload(c);
              }
            } finally {
              rebuildingChunks.remove(key);
            }
          });
          rebuildCount++;
        }
      }
    }
  }

  private void recoverStuckChunks() {
    recoveryCount++;
    if(recoveryCount < 30) return;
    recoveryCount = 0;

    List<Chunk> stuckChunks = new ArrayList<>();
    synchronized(world.chunks) {
      for(Chunk chunk : world.chunks.values()) {
        if(!chunk.uploaded && chunk.hasAllNeighbors(world) && !chunk.hasPendingMesh() && !uploadQueue.contains(chunk)) {
          stuckChunks.add(chunk);
        }
      }
    }
    if(!stuckChunks.isEmpty()) {
      System.out.println("Found " + stuckChunks.size() + " stuck chunks, rebuilding.");
      for(Chunk chunk : stuckChunks) {
        chunk.markDirty();
        CompletableFuture.runAsync(() -> {
          chunk.buildMeshAsync(world);
          queueForUpload(chunk);
        }, executor);
      }
    }
  }

  private CompletableFuture<Chunk> generateChunkWithRetry(int cx, int cz, int attempt) {
    return CompletableFuture.supplyAsync(() -> {
      try {
        return generateChunkInternal(cx, cz);
      } catch (Exception e) {
        if(attempt < Consts.MAX_RETRIES) {
          long delay = Consts.RETRY_DELAYS[attempt];
          System.err.printf("Chunk gen failed for (%d,%d), retry %d in %dms: %s%n",
            cx, cz, attempt + 1, delay, e.getMessage());
          try {
            Thread.sleep(delay);
          } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new CompletionException("Interrupted during retry", ie);
          }
          return generateChunkWithRetry(cx, cz, attempt + 1).join();
        } else {
          System.err.printf("Chunk gen perma failed for (%d,%d): %s%n",
            cx, cz, e.getMessage());
          throw new CompletionException("ChunkGen failed after retries", e);
        }
      }
    }, executor);
  }

  private Chunk generateChunkInternal(int cx, int cz) {
    Chunk existingChunk = world.getChunkIfLoaded(cx, cz);
    if(existingChunk != null) {
      System.err.println("Duplicate generation attempt");
      return existingChunk;
    }
    Chunk chunk = new Chunk(new Vector3i(cx, 0, cz));
    world.generateChunkTerrain(chunk, cx, cz);
    world.addChunkDirectly(chunk);

    chunk.buildMeshAsync(world);
    queueForUpload(chunk);
    if(chunk.hasAllNeighbors(world)) System.out.println("Chunk waiting for neighbor");

    return chunk;
  }

  public CompletableFuture<Chunk> generateChunkAsync(int cx, int cz) {
    if(!world.isInWorldLimit(cx, cz)) return CompletableFuture.completedFuture(null);

    long key = World.chunkKey(cx, cz);

    CompletableFuture<Chunk> existing = generatingChunks.get(key);
    if(existing != null) {
      return existing;
    }

    CompletableFuture<Chunk> future = generateChunkWithRetry(cx, cz, 0);
    future.whenComplete((c, err) -> {
      generatingChunks.remove(key);
      if(err != null) {
        System.err.println("Chunk failed");
      }
    });

    CompletableFuture<Chunk> previous = generatingChunks.putIfAbsent(key, future);
    return previous != null ? previous : future;
  }

  private void markNeighborDirty(Chunk chunk) {
    int cx = chunk.pos.x;
    int cz = chunk.pos.z;

    int[][] offsets = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
    for(int[] off : offsets) {
      Chunk neighbor = world.getChunkIfLoaded(cx + off[0], cz + off[1]);

      if(neighbor != null && !neighbor.uploaded) {
        neighbor.markDirty();
      }
    }
  }

  public void requestChunkGeneration(int cx, int cz) {
    generationQueue.offer(new Vector2i(cx, cz));
  }

  public static void cleanup() {
    if(executor != null) {
      executor.shutdown();
      try {
        if(!executor.awaitTermination(5, TimeUnit.SECONDS)) {
          executor.shutdownNow();
        }
      } catch (Exception e) {
        executor.shutdownNow();
        Thread.currentThread().interrupt();
      }
    }
  }
}
