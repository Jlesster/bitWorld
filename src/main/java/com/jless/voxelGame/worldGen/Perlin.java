package com.jless.voxelGame.worldGen;

import java.util.*;

import com.jless.voxelGame.*;

public class Perlin {

  private static final short[] perm = new short[Consts.PERLIN_SIZE];

  public Perlin(long seed) {
    short[] source = new short[Consts.PERLIN_SIZE];
    for(short i = 0; i < Consts.PERLIN_SIZE; i++) source[i] = i;

    Random rng = new Random(seed);
    for(int i = Consts.PERLIN_SIZE - 1; i >= 0; i--) {
      int j = rng.nextInt(i + 1);
      perm[i] = source[j];
      source[j] = source[i];
    }
  }

  public float noise2D(float x, float y) {
    int xi = fastFloor(x);
    int yi = fastFloor(y);

    float xf = x - xi;
    float yf = y - yi;

    int aa = perm[(perm[xi & Consts.PERLIN_MASK] + yi) & Consts.PERLIN_MASK];
    int ab = perm[(perm[xi & Consts.PERLIN_MASK] + yi + 1) & Consts.PERLIN_MASK];
    int ba = perm[(perm[(xi + 1) & Consts.PERLIN_MASK] + yi) & Consts.PERLIN_MASK];
    int bb = perm[(perm[(xi + 1) & Consts.PERLIN_MASK] + yi + 1) & Consts.PERLIN_MASK];

    float u = fade(xf);
    float v = fade(yf);

    float x1 = lerp(grad(aa, xf, yf), grad(ba, xf - 1, yf), u);
    float x2 = lerp(grad(ab, xf, yf - 1), grad(bb, xf - 1, yf - 1), u);

    return lerp(x1, x2, v);
  }

  private static int fastFloor(float f) {
    return f >= 0 ? (int)f : (int)f - 1;
  }

  private static float fade(float t) {
    return t * t * t * (t * (t * 6 - 15) + 10);
  }

  private static float lerp(float a, float b, float t) {
    return a + t * (b - a);
  }

  private static float grad3D(int hash, float x, float y, float z) {
    int h = hash & 15;
    float u = h < 8 ? x : y;
    float v = h < 4 ? y : (h == 12 || h == 14 ? x : x);
    return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
  }

  private static float grad(int hash, float x, float y) {
    switch(hash & 3) {
      case 0: return   x + y;
      case 1: return  -x + y;
      case 2: return   x - y;
      default: return -x -y;
    }
  }

  public float noise(float x, float y, float z) {
    int xi = fastFloor(x);
    int yi = fastFloor(y);
    int zi = fastFloor(z);

    float xf = x - xi;
    float yf = y - yi;
    float zf = z - zi;

    int aaa = perm[(perm[(perm[xi & Consts.PERLIN_MASK] + yi) & Consts.PERLIN_MASK] + zi) & Consts.PERLIN_MASK];
    int aab = perm[(perm[(perm[xi & Consts.PERLIN_MASK] + yi + 1) & Consts.PERLIN_MASK] + zi) & Consts.PERLIN_MASK];
    int aba = perm[(perm[(perm[xi & Consts.PERLIN_MASK] + yi) & Consts.PERLIN_MASK] + zi + 1) & Consts.PERLIN_MASK];
    int abb = perm[(perm[(perm[xi & Consts.PERLIN_MASK] + yi +1) & Consts.PERLIN_MASK] + zi + 1) & Consts.PERLIN_MASK];

    int baa = perm[(perm[(perm[(xi + 1) & Consts.PERLIN_MASK] + yi) & Consts.PERLIN_MASK] + zi) & Consts.PERLIN_MASK];
    int bab = perm[(perm[(perm[(xi + 1) & Consts.PERLIN_MASK] + yi + 1) & Consts.PERLIN_MASK] + zi) & Consts.PERLIN_MASK];
    int bba = perm[(perm[(perm[(xi + 1) & Consts.PERLIN_MASK] + yi) & Consts.PERLIN_MASK] + zi + 1) & Consts.PERLIN_MASK];
    int bbb = perm[(perm[(perm[(xi + 1) & Consts.PERLIN_MASK] + yi + 1) & Consts.PERLIN_MASK] + zi + 1) & Consts.PERLIN_MASK];

    float u = fade(xf);
    float v = fade(yf);
    float w = fade(zf);

    float x1 = lerp(grad3D(aaa, xf, yf, zf), grad3D(baa, xf - 1, yf, zf), u);
    float x2 = lerp(grad3D(aba, xf, yf - 1, zf), grad3D(bba, xf - 1, yf - 1, zf), u);
    float x3 = lerp(grad3D(aab, xf, yf, zf - 1), grad3D(bab, xf - 1, yf, zf - 1), u);
    float x4 = lerp(grad3D(abb, xf, yf - 1, zf - 1), grad3D(bbb, xf - 1, yf - 1, zf - 1), u);

    float y1 = lerp(x1, x2, v);
    float y2 = lerp(x3, x4, v);

    return lerp(y1, y2, w);
  }
}
