package com.jless.voxelGame.world;

import java.util.Random;

public class Perlin {

  private final int[] perm = new int[512];

  public Perlin(long seed) {
    int[] p = new int[256];
    for(int i = 0; i < 256; i++) p[i] = i;

    Random rng = new Random(seed);

    for(int i = 255; i > 0; i--) {
      int j = rng.nextInt(i + 1);
      int tmp = p[i];
      p[i] = p[j];
      p[j] = tmp;
    }
    for(int i = 0; i < 512; i++) {
      perm[i] = p[i & 255];
    }
  }

  public float noise(float x, float y) {
    int X = fastFloor(x) & 255;
    int Y = fastFloor(y) & 255;

    float xf = x - fastFloor(x);
    float yf = y - fastFloor(y);

    float u = fade(xf);
    float v = fade(yf);

    int aa = perm[X + perm[Y]];
    int ab = perm[X + perm[Y + 1]];
    int ba = perm[X + 1 + perm[Y]];
    int bb = perm[X + 1 + perm[Y + 1]];

    float x1 = lerp(grad(aa, xf, yf), grad(ba, xf - 1, yf), u);
    float x2 = lerp(grad(ab, xf, yf - 1), grad(bb, xf - 1, yf - 1), u);

    return lerp(x1, x2, v);
  }

  public float fbm(float x, float y, int octaves, float persistence, float lacunarity) {
    float amp = 1.0f;
    float freq = 1.0f;
    float sum = 0.0f;
    float max = 0.0f;

    for(int i = 0; i < octaves; i++) {
      sum += noise(x * freq, y * freq) * amp;
      max += amp;

      amp *= persistence;
      freq *= lacunarity;
    }
    return sum / max;
  }

  private static float grad(int hash, float x, float y) {
    switch(hash & 7) {
      case 0: return x + y;
      case 1: return -x + y;
      case 2: return x - y;
      case 3: return -x - y;
      case 4: return x;
      case 5: return -x;
      case 6: return y;
      case 7: return -y;
    }
    return 0;
  }

  private static int fastFloor(float f) {
    return f >= 0 ? (int)f : (int)f - 1;
  }

  private static float lerp(float a, float b, float t) {
    return a + t * (b - a);
  }

  private static float fade(float t) {
    return t * t * t * (t * (t * 6 - 15) + 10);
  }
}
