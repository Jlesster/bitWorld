package com.jless.voxelGame.worldGen.structureGen.treeGen;

import java.util.*;

import com.jless.voxelGame.blocks.*;
import com.jless.voxelGame.worldGen.*;

public abstract class TreeGenerator {

  protected Random random;

  public TreeGenerator(long seed) {
    this.random = new Random(seed);
  }

  protected boolean canPlaceTree(World world, int x, int y, int z, int height) {
    if(y < 1 || y + height > 250) return false;

    byte groundBlock = world.getIfLoaded(x, y - 1, z);
    if(groundBlock != BlockID.GRASS && groundBlock != BlockID.DIRT) return false;

    for(int dy = 0; dy < height; dy++) {
      byte block = world.getIfLoaded(x, y + dy, z);
      if(block != BlockID.AIR) return false;
    }
    return true;
  }

  protected void placeLog(World world, int x, int y, int z, byte logType) {
    world.set(x, y, z, logType);
  }

  protected void placeLeaves(World world, int x, int y, int z, byte leafType) {
    byte existing = world.getIfLoaded(x, y, z);
    if(existing == BlockID.AIR) world.set(x, y, z, leafType);
  }

  public abstract void generate(World world, int x, int y, int z);
}
