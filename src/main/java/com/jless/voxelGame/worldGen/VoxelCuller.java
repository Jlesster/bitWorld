package com.jless.voxelGame.worldGen;

import com.jless.voxelGame.*;
import com.jless.voxelGame.blocks.*;
import com.jless.voxelGame.chunkGen.*;

public class VoxelCuller {

  public static final int[][] DIRS = {
  {1, 0, 0}, {-1, 0, 0},
  {0, 1, 0}, {0, -1, 0},
  {0, 0, 1}, {0, 0, -1}
  };

  public static boolean isFaceVisible(BlockMap map, int x, int y, int z, int faceID) {
    int[] d = DIRS[faceID];
    byte neighbor = map.get(x + d[0], y + d[1], z + d[2]);
    return !Blocks.SOLID[neighbor];
  }

  public static boolean isFaceVisible(World world, int wX, int wY, int wZ, int faceID) {
    int[] d = DIRS[faceID];
    byte neighbor = world.getIfLoaded(wX + d[0], wY + d[1], wZ + d[2]);
    return !Blocks.SOLID[neighbor];
  }

  public static boolean isFaceVisible(Chunk chunk, World world, int lX, int lY, int lZ, int faceID) {
    int wX = chunk.pos.x * Consts.CHUNK_SIZE + lX;
    int wY = lY;
    int wZ = chunk.pos.z * Consts.CHUNK_SIZE + lZ;
    return isFaceVisible(world, wX, wY, wZ, faceID);
  }
}
