package com.jless.voxelGame;

import java.nio.*;
import java.util.concurrent.*;

import org.joml.*;

import com.jless.voxelGame.chunkGen.*;
import com.jless.voxelGame.worldGen.*;

public class ChunkThreadManager {

  private final World world;
  private static ExecutorService executor;
  private final ConcurrentLinkedQueue<ChunkUpload> uploadQueue;
  private final ConcurrentHashMap<Long, CompletableFuture<Chunk>> generatingChunks;

  ChunkThreadManager(World world) {
    this.world = world;
    ChunkThreadManager.executor = Executors.newFixedThreadPool(Consts.THREAD_COUNT);
    this.uploadQueue = new ConcurrentLinkedQueue<>();
    this.generatingChunks = new ConcurrentHashMap<>();
  }

  public void queueForUpload(Chunk chunk, FloatBuffer meshData) {
    uploadQueue.offer(new ChunkUpload(chunk, meshData));
  }

  public synchronized void processUploads() {
    int processed = 0;
    while(!uploadQueue.isEmpty()) {
      ChunkUpload upload = uploadQueue.poll();
      if(upload != null) {
        uploadToGPU(upload);
        processed++;
      }
    }
    if(processed > 0) {
      System.out.println("Processed " + processed + " chunks uploads on main thread");
    }
  }

  private void uploadToGPU(ChunkUpload upload) {
    world.addChunkDirectly(upload.chunk);
    markNeighborDirty(upload.chunk);
    FloatBuffer data = upload.meshData;
    if(data == null) {
      data = upload.chunk.buildMesh(world);
    }

    if(data == null || data.limit() == 0) {
      System.err.println("Mesh empty");
      return;
    }
    upload.chunk.uploadToGPU(data);
    long key = World.chunkKey(upload.chunk.pos.x, upload.chunk.pos.z);
    generatingChunks.remove(key);

    System.out.println("Uploaded chunk at (" + upload.chunk.pos.x + ", " + upload.chunk.pos.z + ") with " + upload.chunk.vertCount + " vertices");
    System.out.println("World now contains " + world.chunks.size() + " chunks");
  }

  private CompletableFuture<Chunk> generateChunkWithRetry(int cx, int cz, int attempt) {
    long key = World.chunkKey(cx, cz);
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
    Chunk chunk = new Chunk(new Vector3i(cx, 0, cz));
    GenerateTerrain.fillChunk(chunk, world);
    FloatBuffer meshData = chunk.buildMesh(world);
    queueForUpload(chunk, null);
    return chunk;
  }

  public CompletableFuture<Chunk> generateChunkAsync(int cx, int cz) {
    long key = World.chunkKey(cx, cz);

    CompletableFuture<Chunk> existing = generatingChunks.get(key);
    if(existing != null) {
      return existing;
    }

    CompletableFuture<Chunk> future = generateChunkWithRetry(cx, cz, 0);

    CompletableFuture<Chunk> previous = generatingChunks.putIfAbsent(key, future);
    return previous != null ? previous : future;
  }

  private void markNeighborDirty(Chunk chunk) {
    int cx = chunk.pos.x;
    int cz = chunk.pos.z;

    Chunk neighbor;

    neighbor = world.getChunkIfLoaded(cx + 1, cz);
    if(neighbor != null) neighbor.markDirty();

    neighbor = world.getChunkIfLoaded(cx - 1, cz);
    if(neighbor != null) neighbor.markDirty();

    neighbor = world.getChunkIfLoaded(cx, cz + 1);
    if(neighbor != null) neighbor.markDirty();

    neighbor = world.getChunkIfLoaded(cx, cz - 1);
    if(neighbor != null) neighbor.markDirty();

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
