package com.jless.voxelGame.worldGen;

import com.jless.voxelGame.*;
import com.jless.voxelGame.blocks.*;
import com.jless.voxelGame.chunkGen.*;

public class GenerateTerrain {

  public static void fillChunk(Chunk chunk, World world) {
    BlockMap map = chunk.getBlockMap();

    int wXstart = chunk.pos.x * Consts.CHUNK_SIZE;
    int wZstart = chunk.pos.z * Consts.CHUNK_SIZE;

    for(int x = 0; x < Consts.CHUNK_SIZE; x ++) {
      for(int z = 0; z < Consts.CHUNK_SIZE; z++) {
        int wX = wXstart + x;
        int wZ = wZstart + z;

        int surfHeight = (int)calculateTerrainHeight(wX, wZ);
        fillTerrainColumn(x, z, surfHeight, map);
      }
    }
  }

  private static void fillTerrainColumn(int x, int z, int surfHeight, BlockMap map) {
    for(int y = surfHeight + 1; y < Consts.WORLD_HEIGHT; y++) {
      map.set(x, y, z, BlockID.AIR);
    }

    if(surfHeight > 0) {
      map.set(x, surfHeight, z, BlockID.GRASS);
    }

    for(int y = surfHeight - 3; y < surfHeight && y > 0; y++) {
      map.set(x, y, z, BlockID.DIRT);
    }

    for(int y = 2; y < surfHeight - 3 && y > 0; y++) {
      map.set(x, y, z, BlockID.STONE);
    }

    map.set(x, 0, z, BlockID.BEDROCK);
    map.set(x, 1, z, BlockID.BEDROCK);
  }

  private static float calculateTerrainHeight(int worldX, int worldZ) {
    float height = 0f;
    float amp = 1f;
    float freq = 0.005f;

    for(int octave = 0; octave < 4; octave++) {
      height += ThreadSafePerlin.noise2D(worldX * freq, worldZ * freq) *amp;
      amp *= 0.5f;
      freq *= 2;
    }
    return (height + 1f) * 0.5f * 80f + 40f;
  }
}
