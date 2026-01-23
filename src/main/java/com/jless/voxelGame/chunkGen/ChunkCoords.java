package com.jless.voxelGame.chunkGen;

import com.jless.voxelGame.*;

public class ChunkCoords {

  public ChunkCoords() {}

  public static int chunk(int wC) {
    return Math.floorDiv(wC, Consts.CHUNK_SIZE);
  }

  public static int local(int wC) {
    return Math.floorMod(wC, Consts.CHUNK_SIZE);
  }
}
