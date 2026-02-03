package com.jless.voxelGame.worldGen.structureGen.caveGen;

import java.util.*;

import com.jless.voxelGame.*;
import com.jless.voxelGame.blocks.*;
import com.jless.voxelGame.worldGen.*;
import com.jless.voxelGame.worldGen.noiseGen.*;

public class WormCaveGenerator {

  private MultiOctaveNoise caveNoise;
  private Random random;

  private static final float CAVE_THRESH = 0.6f;
  private static final float CAVE_SCALE = 40.0f;

  public WormCaveGenerator(long seed) {
    this.caveNoise = new MultiOctaveNoise(seed, 4);
    this.random = new Random(seed);
  }

  public void carveCaves(World world, int cx, int cz) {
    int worldX = cx * Consts.CHUNK_SIZE;
    int worldZ = cz * Consts.CHUNK_SIZE;

    for(int x = 0; x < Consts.CHUNK_SIZE; x++) {
      for(int z = 0; z < Consts.CHUNK_SIZE; z++) {
        int wx = worldX + x;
        int wz = worldZ + z;

        int surfY = world.getSurfY(wx, wz);
        int minY = 5;
        int maxY = surfY - 5;

        for(int y = minY; y < maxY; y++) {
          double noise = caveNoise.getNoise3D(wx, y, wz, CAVE_SCALE, 0.6);
          double verticalBias = 1.0 - Math.abs((y - 64.0) / 64.0);
          noise *= verticalBias;

          if(noise > CAVE_THRESH) {
            byte currBlock = world.getIfLoaded(wx, y, wz);
            if(Blocks.SOLID[currBlock]) world.set(wx, y, wz, BlockID.AIR);
          }
        }
      }
    }
  }
}
