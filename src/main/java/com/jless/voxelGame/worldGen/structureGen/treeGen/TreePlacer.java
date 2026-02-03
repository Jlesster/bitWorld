package com.jless.voxelGame.worldGen.structureGen.treeGen;

import java.util.*;

import com.jless.voxelGame.*;
import com.jless.voxelGame.worldGen.*;
import com.jless.voxelGame.worldGen.biomeGen.*;

public class TreePlacer {

  private OakTreeGenerator oakGen;
  private SpruceTreeGenerator spruceGen;
  private BiomeGenerator biomeGen;
  private Random random;

  public TreePlacer(long seed, BiomeGenerator biomeGen) {
    this.biomeGen = biomeGen;
    this.oakGen = new OakTreeGenerator(seed);
    this.spruceGen = new SpruceTreeGenerator(seed);
    this.random = new Random(seed);
  }

  public void placeTrees(World world, int cx, int cz) {
    long chunkSeed = (long)cx * 341873128712L + (long)cz * 132897987541L;
    random.setSeed(chunkSeed);

    int wx = cx * Consts.CHUNK_SIZE;
    int wz = cz * Consts.CHUNK_SIZE;

    int attempts = 8;

    for(int i = 0; i < attempts; i++) {
      int dx = random.nextInt(16);
      int dz = random.nextInt(16);
      int x = wx + dx;
      int z = wz + dz;

      BiomeType biome = biomeGen.getBiome(x, z);
      double treeDensity = biome.getTreeDensity();

      if(random.nextDouble() < treeDensity) {
        int y = world.getSurfY(x, z);

        if(y > 0 && y < 240) {
          TreeType treeType = biome.getPrimaryTree();
          placeTree(world, x, y + 1, z, treeType);
        }
      }
    }
  }

  private void placeTree(World world, int x, int y, int z, TreeType type) {
    switch(type) {
      case OAK:
        oakGen.generate(world, x, y, z);
        break;
      case SPRUCE:
        spruceGen.generate(world, x, y, z);
        break;
    }
  }
}
