package com.jless.voxelGame.worldGen.noiseGen;

import java.util.*;

public class MultiOctaveNoise {

  private SimplexNoise[] octaves;
  private float[] frequencies;
  private float[] amplitudes;
  private int octaveCount;

  public MultiOctaveNoise(long seed, int octaveCount) {
    this.octaveCount = octaveCount;
    this.octaves = new SimplexNoise[octaveCount];
    this.frequencies = new float[octaveCount];
    this.amplitudes = new float[octaveCount];

    Random rand = new Random(seed);

    for(int i = 0; i < octaveCount; i++) {
      octaves[i] = new SimplexNoise(rand.nextLong());
      frequencies[i] = (float)Math.pow(2, i);
      amplitudes[i] = (float)Math.pow(0.5, i);
    }
  }

  public double getNoise(int x, int z, float scale, double persistence) {
    double total = 0;
    double maxValue = 0;

    for(int i = 0; i < octaveCount; i++) {
      float frequency = frequencies[i] / scale;
      float amplitude = (float)Math.pow(persistence, i);

      total += octaves[i].noise(x * frequency, z * frequency) * amplitude;
      maxValue += amplitude;
    }
    return total / maxValue;
  }

  public double getNoise3D(int x, int y, int z, float scale, double persistence) {
    double total = 0;
    double maxValue = 0;

    for(int i = 0; i < octaveCount; i++) {
      float frequency = frequencies[i] / scale;
      double amplitude = Math.pow(persistence, i);

      total += octaves[i].noise(x * frequency, y * frequency, z * frequency) * amplitude;
      maxValue += amplitude;
    }
    return total / maxValue;
  }

  public double getNoiseCustom(int x, int z, float baseScale, float[] customFreqs, float[] customAmps) {
    double total = 0;
    double maxValue = 0;

    int count = Math.min(octaveCount, Math.min(customFreqs.length, customAmps.length));

    for(int i = 0; i < count; i++) {
      float frequency = customFreqs[i] / baseScale;
      float amplitude = customAmps[i];

      total += octaves[i].noise(x * frequency, z * frequency) * amplitude;
      maxValue += amplitude;
    }
    return total / maxValue;
  }
}
