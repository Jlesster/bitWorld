package com.jless.voxelGame.worldGen.oreGen;

import java.util.*;

import com.jless.voxelGame.blocks.*;
import com.jless.voxelGame.worldGen.*;

public class OreVeinGenerator {

  private Random random;

  public OreVeinGenerator(long seed) {
    this.random = new Random(seed);
  }

  public void generateVein(World world, int sx, int sy, int sz, OreConfig config) {
    if(sx < config.minHeight || sy > config.maxHeight) return;

    double angle = random.nextDouble() * Math.PI;
    double horizAngle = random.nextDouble() * Math.PI * 2;

    int blocksPlaced = 0;
    int maxBlocks = config.veinSize;

    double x = sx;
    double y = sy;
    double z = sz;

    for(int i = 0; i < maxBlocks; i++) {
      int ix = (int)Math.round(x);
      int iy = (int)Math.round(y);
      int iz = (int)Math.round(z);

      if(canPlaceOre(world, ix, iy, iz, config)) {
        world.set(ix, iy, iz, config.oreBlock);
        blocksPlaced++;
      }

      x += Math.sin(angle) * Math.cos(horizAngle) * 0.7;
      y += Math.cos(angle) * 0.7;
      z += Math.sin(angle) * Math.sin(horizAngle) * 0.7;

      angle += (random.nextDouble() - 0.5) * 0.3;
      horizAngle += (random.nextDouble() - 0.5) * 0.3;

      double dist = Math.sqrt(
        (x - sx) * (x - sx) +
        (y - sy) * (y - sy) +
        (z - sz) * (z - sz)
      );

      if(dist > maxBlocks * 0.8) break;
    }
  }

  private boolean canPlaceOre(World world, int x, int y, int z, OreConfig config) {
    if(y < config.minHeight || y > config.maxHeight) return false;

    byte block = world.getIfLoaded(x, y, z);
    return block == BlockID.STONE;
  }
}
