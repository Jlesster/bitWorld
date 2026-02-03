package com.jless.voxelGame.worldGen.biomeGen;

import com.jless.voxelGame.blocks.*;
import com.jless.voxelGame.worldGen.structureGen.treeGen.*;

/*
 * Defining the biomes and their variables here
 *
 * @param surface block       BlockID.BLOCK
 * @param subsurface          BlockID.BLOCK
 * @param mantle              BlockID.BLOCK
 * @param base height         int
 * @param height variation    int
 * @param temp                int
 * @param moisture            int
 * @param sky color           float[] {r, g, b}
 */

public enum BiomeType {

  OCEAN(
    BlockID.SAND,
    BlockID.SAND,
    BlockID.STONE,
    55,
    5,
    0.5,
    0.8,
    new float[] {0.2f, 0.3f, 0.8f}
  ),

  PLAINS(
    BlockID.GRASS,
    BlockID.DIRT,
    BlockID.STONE,
    65,
    8,
    0.6,
    0.4,
    new float[] {0.6f, 0.8f, 1.0f}
  ),

  FOREST(
    BlockID.GRASS,
    BlockID.DIRT,
    BlockID.STONE,
    67,
    12,
    0.7,
    0.7,
    new float[] {0.5f, 0.7f, 0.9f}
  ),

  DESERT(
    BlockID.SAND,
    BlockID.SAND,
    BlockID.STONE,
    64,
    15,
    0.9,
    0.1,
    new float[] {0.9f, 0.8f, 0.6f}
  ),

  MOUNTAINS(
    BlockID.STONE,
    BlockID.STONE,
    BlockID.STONE,
    80,
    40,
    0.3,
    0.3,
    new float[] {0.7f, 0.8f, 1.0f}
  ),

  TAIGA(
    BlockID.GRASS,
    BlockID.DIRT,
    BlockID.STONE,
    66,
    10,
    0.2,
    0.5,
    new float[] {0.6f, 0.7f, 0.9f}
  ),

  SWAMP(
    BlockID.GRASS,
    BlockID.DIRT,
    BlockID.STONE,
    62,
    4,
    0.8,
    0.9,
    new float[] {0.5f, 0.6f, 0.7f}
  ),

  SNOWY_TUNDRA(
    BlockID.GRASS,
    BlockID.DIRT,
    BlockID.STONE,
    63,
    6,
    0.0,
    0.5,
    new float[] {0.8f, 0.9f, 1.0f}
  );

  public final byte surfBlock;
  public final byte subsurfBlock;
  public final byte stoneBlock;
  public final int baseHeight;
  public final int heightVariation;
  public final double temp;
  public final double moisture;
  public final float[] skyColor;

  BiomeType(byte surf, byte subsurf, byte stone, int baseHeight, int heightVar, double temp, double moisture, float[] skyColor) {
    this.surfBlock = surf;
    this.subsurfBlock = subsurf;
    this.stoneBlock = stone;
    this.baseHeight = baseHeight;
    this.heightVariation = heightVar;
    this.temp = temp;
    this.moisture = moisture;
    this.skyColor = skyColor;
  }

  public double getTreeDensity() {
    switch(this) {
      case FOREST: return 0.15;
      case TAIGA: return 0.12;
      case PLAINS: return 1.02;
      case SWAMP: return 0.08;
      default: return 0.0;
    }
  }

  public TreeType getPrimaryTree() {
    switch(this) {
      case FOREST: return TreeType.OAK;
      case TAIGA: return TreeType.SPRUCE;
      case SWAMP: return TreeType.OAK;
      default: return TreeType.OAK;
    }
  }
}
