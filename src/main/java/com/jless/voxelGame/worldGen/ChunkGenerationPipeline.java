package com.jless.voxelGame.worldGen;

import java.util.*;

import com.jless.voxelGame.chunkGen.*;

public class ChunkGenerationPipeline {

  private WorldGenerator worldGen;
  private World world;

  private Set<Long> generatedChunks;
  private Queue<ChunkCoord> postProcessQueue;

  private static class ChunkCoord {
    int x, z;
    ChunkCoord(int x, int z) {
      this.x = x;
      this.z = z;
    }
  }

  public ChunkGenerationPipeline(long seed, World world) {
    this.worldGen = new WorldGenerator(seed);
    this.world = world;
    this.generatedChunks = new HashSet<>();
    this.postProcessQueue = new LinkedList<>();
  }

  public void generateChunk(int cx, int cz) {
    long key = chunkKey(cx, cz);
    if(generatedChunks.contains(key)) return;

    Chunk chunk = new Chunk(cx, cz);
    worldGen.generateChunk(chunk, cx, cz);
    world.addChunkDirectly(chunk);
    generatedChunks.add(key);

    postProcessQueue.add(new ChunkCoord(cx, cz));
    processPostProcessQueue();
  }

  private void processPostProcessQueue() {
    Iterator<ChunkCoord> iter = postProcessQueue.iterator();

    while(iter.hasNext()) {
      ChunkCoord coord = iter.next();

      if(hasAllNeighbors(coord.x, coord.z)) {
        worldGen.postProcessChunk(world, coord.x, coord.z);
        iter.remove();
      }
    }
  }

  private boolean hasAllNeighbors(int cx, int cz) {
    for(int dx = -1; dx <= 1; dx++) {
      for(int dz = -1; dz <= 1; dz++) {
        long key = chunkKey(cx + dx, cz + dz);
        if(!generatedChunks.contains(key)) return false;
      }
    }
    return true;
  }

  private long chunkKey(int x, int z) {
    return ((long)x << 32) | (z & 0xFFFFFFFFL);
  }
}
