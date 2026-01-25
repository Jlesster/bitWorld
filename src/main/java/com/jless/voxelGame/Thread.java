package com.jless.voxelGame;

import java.nio.*;
import java.util.concurrent.*;

import org.joml.*;

import com.jless.voxelGame.chunkGen.*;
import com.jless.voxelGame.worldGen.*;

public class Thread {

  private final World world;
  private final ExecutorService executor;
  private final ConcurrentLinkedQueue<ChunkUpload> uploadQueue;
  private final ConcurrentHashMap<Long, CompletableFuture<Chunk>> generatingChunks;

  Thread(World world) {
    this.world = world;
    this.executor = Executors.newFixedThreadPool(Consts.THREAD_COUNT);
    this.uploadQueue = new ConcurrentLinkedQueue<>();
    this.generatingChunks = new ConcurrentHashMap<>();
  }

  public void queueForUpload(Chunk chunk, FloatBuffer meshData) {
    uploadQueue.offer(new ChunkUpload(chunk, meshData));
  }

  public void processUploads() {
    while(!uploadQueue.isEmpty()) {
      ChunkUpload upload = uploadQueue.poll();
      uploadToGPU(upload);
    }
  }

  private void uploadToGPU(ChunkUpload upload) {
    upload.chunk.uploadToGPU(upload.meshData);
    long key = World.chunkKey(upload.chunk.pos.x, upload.chunk.pos.z);
    generatingChunks.remove(key);
  }

  public CompletableFuture<Chunk> generateChunkAsync(int cx, int cz) {
    long key = World.chunkKey(cx, cz);

    CompletableFuture<Chunk> existing = generatingChunks.get(key);
    if(existing != null) {
      return existing;
    }

    CompletableFuture<Chunk> future = CompletableFuture.supplyAsync(() -> {
      Chunk chunk = new Chunk(new Vector3i(cx, 0, cz));
      GenerateTerrain.fillChunk(chunk, world);
      chunk.ensureUploaded(world);
      return chunk;
    }, executor);
    generatingChunks.put(key, future);
    return future;
  }


}
