package com.jless.voxelGame.worldGen;

import com.jless.voxelGame.blocks.*;

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
}
