package com.jless.voxelGame.worldGen.structureGen.caveGen;

import com.jless.voxelGame.worldGen.*;

public class CaveSystem {

  private WormCaveGenerator wormCaves;
  private CavernGenerator caverns;

  public CaveSystem(long seed) {
    this.wormCaves = new WormCaveGenerator(seed);
    this.caverns = new CavernGenerator(seed);
  }

  public void generateCaves(World world, int cx, int cz) {
    wormCaves.carveCaves(world, cx, cz);
    caverns.generateCaverns(world, cx, cz);
  }
}
