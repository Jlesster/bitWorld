package com.jless.voxelGame.worldGen.oreGen;

import com.jless.voxelGame.blocks.*;

public class OreConfig {

  public final byte oreBlock;
  public final int minHeight;
  public final int maxHeight;
  public final int veinSize;
  public final int veinsPerChunk;
  public final double rarity;

  public OreConfig(byte ore, int minh, int maxh, int veinSize, int veinsPerChunk, double rarity) {
    this.oreBlock = ore;
    this.minHeight = minh;
    this.maxHeight = maxh;
    this.veinSize = veinSize;
    this.veinsPerChunk = veinsPerChunk;
    this.rarity = rarity;
  }

  public static final OreConfig COAL = new OreConfig(
    BlockID.COAL_ORE, 5, 128, 8, 20, 1.0
  );

  public static final OreConfig IRON = new OreConfig(
    BlockID.IRON_ORE, 5, 64, 6, 15, 0.9
  );

  public static final OreConfig COPPER = new OreConfig(
    BlockID.COPPER_ORE, 5, 96, 7, 12, 0.85
  );

  public static final OreConfig DIAMOND = new OreConfig(
    BlockID.DIAMOND_ORE, 5, 16, 4, 3, 0.6
  );

  public static OreConfig[] getAllOres() {
    return new OreConfig[] {
      COAL, IRON, COPPER, DIAMOND
    };
  }
}
