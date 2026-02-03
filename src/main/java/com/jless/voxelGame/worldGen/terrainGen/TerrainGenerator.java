package com.jless.voxelGame.worldGen.terrainGen;

import com.jless.voxelGame.worldGen.biomeGen.*;
import com.jless.voxelGame.worldGen.noiseGen.*;

public class TerrainGenerator {

  private BiomeGenerator biomeGen;
  private MultiOctaveNoise continentNoise;
  private MultiOctaveNoise terrainNoise;
  private MultiOctaveNoise detailNoise;

  private static final float CONTINENT_SCALE = 2000.0f;
  private static final float TERRAIN_SCALE = 300.0f;
  private static final float DETAIL_SCALE = 80.0f;

  public TerrainGenerator(long seed, BiomeGenerator biomeGen) {
    this.biomeGen = biomeGen;
    this.continentNoise = new MultiOctaveNoise(seed, 4);
    this.terrainNoise = new MultiOctaveNoise(seed + 100, 5);
    this.detailNoise = new MultiOctaveNoise(seed + 200, 3);
  }

  public int getHeight(int x, int z) {
    BiomeType biome = biomeGen.getBiome(x, z);
    double continent = terrainNoise.getNoise(x, z, CONTINENT_SCALE, 0.5);
    double terrain = terrainNoise.getNoise(x, z, TERRAIN_SCALE, 0.55);
    double detail = detailNoise.getNoise(x, z, DETAIL_SCALE, 0.6);
    double height = biome.baseHeight;

    height += continent * 15;
    height += terrain * biome.heightVariation;
    height += detail * 4;

    if(biome == BiomeType.MOUNTAINS) {
      double mountainNoise = terrainNoise.getNoise(x, z, 200.0f, 0.65);
      height += Math.max(0, mountainNoise * 30);
    }

    if(height < 60) {
      return (int)height;
    }
    return (int)height;
  }

  public int getSufDepth(int x, int z) {
    BiomeType biome = biomeGen.getBiome(x, z);

    if(biome == BiomeType.DESERT) {
      return 4 + (int)(detailNoise.getNoise(x, z, 50.0f, 0.5) * 2);
    }
    return 1;
  }

  public int getSubsurfDepth(int x, int z) {
    return 3 + (int)(detailNoise.getNoise(x + 1000, z + 1000, 40.0f, 0.5) * 2);
  }
}
