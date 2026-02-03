package com.jless.voxelGame.worldGen.biomeGen;

import com.jless.voxelGame.worldGen.noiseGen.*;

public class BiomeGenerator {

  private MultiOctaveNoise tempNoise;
  private MultiOctaveNoise moistNoise;
  private MultiOctaveNoise biomeVariationNoise;

  private static final float TEMP_SCALE = 800.0f;
  private static final float MOIST_SCALE = 600.0f;
  private static final float VAR_SCALE = 400.0f;

  public BiomeGenerator(long seed) {
    this.tempNoise = new MultiOctaveNoise(seed, 4);
    this.moistNoise = new MultiOctaveNoise(seed + 1000, 4);
    this.biomeVariationNoise = new MultiOctaveNoise(seed + 2000, 3);
  }

  public BiomeType getBiome(int x, int z) {
    double temp = getTemperature(x, z);
    double moisture = getMoisture(x, z);

    return selectBiome(temp, moisture);
  }

  public double getTemperature(int x, int z) {
    double noise = tempNoise.getNoise(x, z, TEMP_SCALE, 0.5);
    return (noise + 1.0) * 0.5;
  }

  public double getMoisture(int x, int z) {
    double noise = moistNoise.getNoise(x, z, MOIST_SCALE, 0.5);
    return (noise + 1.0) * 0.5;
  }

  public double getBiomeVariation(int x, int z) {
    return biomeVariationNoise.getNoise(x, z, VAR_SCALE, 0.6);
  }

/*
* Biome distribution map:
*           Dry (0)    Medium     Wet (1)
* Cold (0)  Tundra     Taiga      Taiga
* Medium    Desert     Plains     Forest
* Hot (1)   Desert     Plains     Swamp
*
*/
  private BiomeType selectBiome(double temp, double moist) {
    if(temp < 0.25) {         //cold
      if(moist < 0.33) {
        return BiomeType.SNOWY_TUNDRA;
      } else {
        return BiomeType.TAIGA;
      }
    } else if(temp < 0.75) {  //temperate
      if(moist < 0.33) {
        return BiomeType.DESERT;
      } else if(moist < 0.66) {
        return BiomeType.PLAINS;
      } else {
        return BiomeType.FOREST;
      }
    } else {                  //Hot biomes
      if(moist < 0.5) {
        return BiomeType.DESERT;
      } else if(moist < 0.75) {
        return BiomeType.PLAINS;
      } else {
        return BiomeType.SWAMP;
      }
    }
  }

  public int getBlendedheight(int x, int z, int baseHeight) {
    final int sampleRadius = 8;
    double totalHeight = 0;
    double totalWeight = 0;

    for(int dx = -sampleRadius; dx <= sampleRadius; dx++) {
      for(int dz = -sampleRadius; dz <= sampleRadius; dz++) {
        BiomeType biome = getBiome(x + dx, z + dz);
        double distance = Math.sqrt(dx * dx + dz * dz);
        double weight = 1.0 / (1.0 + distance);

        totalHeight += biome.baseHeight * weight;
        totalWeight += weight;
      }
    }
    return (int)(totalHeight / totalWeight);
  }
}
