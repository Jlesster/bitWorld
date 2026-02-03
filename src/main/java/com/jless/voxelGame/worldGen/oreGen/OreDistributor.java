package com.jless.voxelGame.worldGen.oreGen;

import java.util.*;

import com.jless.voxelGame.*;
import com.jless.voxelGame.worldGen.*;

public class OreDistributor {

  private OreVeinGenerator veinGen;
  private OreConfig[] oreConfigs;
  private Random random;

  public OreDistributor(long seed) {
    this.veinGen = new OreVeinGenerator(seed);
    this.oreConfigs =OreConfig.getAllOres();
    this.random = new Random(seed);
  }

  public void distributeOres(World world, int cx, int cz) {
    long chunkSeed = (long)cx * 341873128712L + (long)cz * 132897987541L;
    random.setSeed(chunkSeed);

    int worldX = cx * Consts.CHUNK_SIZE;
    int worldZ = cz * Consts.CHUNK_SIZE;

    for(OreConfig config : oreConfigs) {
      int attempts = config.veinsPerChunk;

      for(int i = 0; i < attempts; i++) {
        if(random.nextDouble() > config.rarity) continue;

        int x = worldX + random.nextInt(16);
        int z = worldZ + random.nextInt(16);
        int y = config.minHeight + random.nextInt(
          config.maxHeight - config.minHeight + 1
        );

        veinGen.generateVein(world, x, y, z, config);
      }
    }
  }
}
