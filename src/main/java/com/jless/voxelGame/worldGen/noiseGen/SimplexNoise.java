package com.jless.voxelGame.worldGen.noiseGen;

import java.util.*;

public class SimplexNoise {

  private int[] perm;
  private static final int[][] grad3 = {
    {1, 1, 0}, {-1, 1, 0}, {1, -1, 0}, {-1, -1, 0},
    {1, 0, 1}, {-1, 0, 1}, {1, 0, -1}, {-1, 0, -1},
    {0, 1, 1}, {0, -1, 1}, {0, 1, -1}, {0, -1, -1}
  };

  public SimplexNoise(long seed) {
    Random rand = new Random(seed);
    perm = new int[512];

    int[] p = new int[256];
    for(int i = 0; i < 256; i++) {
      p[i] = i;
    }

    for(int i = 255; i > 0; i--) {
      int j = rand.nextInt(i + 1);
      int temp = p[i];
      p[i] = p[j];
      p[j] = temp;
    }

    for(int i = 0; i < 512; i++) {
      perm[i] = p[i & 255];
    }
  }

  public double noise(float x, float z) {
    final float F2 = 0.5f * ((float)Math.sqrt(3.0f) - 1.0f);
    final float G2 = (3.0f - (float)Math.sqrt(3.0f)) / 6.0f;

    float s = (x + z) * F2;
    int i = fastFloor(x + s);
    int j = fastFloor(z + s);

    double t = (i + j) * G2;
    double X0 = i - t;
    double Y0 = j - t;
    double x0 = x - X0;
    double y0 = z - Y0;

    int i1, j1;
    if(x0 > y0) {
      i1 = 1;
      j1 = 0;
    } else {
      i1 = 0;
      j1 = 1;
    }

    double x1 = x0 - i1 + G2;
    double y1 = y0 - j1 + G2;
    double x2 = x0 - 1.0 + 2.0 * G2;
    double y2 = y0 - 1.0 + 2.0 * G2;

    int ii = i & 255;
    int jj = j & 255;
    int gi0 = perm[(ii + perm[jj]) & 511] % 12;
    int gi1 = perm[(ii + i1 + perm[(jj + j1) & 511]) & 511] % 12;
    int gi2 = perm[(ii + 1 + perm[(jj + 1) & 511]) & 511] % 12;

    double n0 = 0, n1 = 0, n2 = 0;
    double t0 = 0.5 - x0 * x0 - y0 * y0;
    if(t0 >= 0) {
      t0 *= t0;
      n0 = t0 * t0 * dot(grad3[gi0], x0, y0);
    }

    double t1 = 0.5 - x1 * x1 - y1 * y1;
    if(t1 >= 0) {
      t1 *= t1;
      n1 = t1 * t1 * dot(grad3[gi1], x1, y1);
    }

    double t2 = 0.5 - x2 * x2 - y2 * y2;
    if(t2 >= 0) {
      t2 *= t2;
      n2 = t2 * t2 * dot(grad3[gi2], x2, y2);
    }

    return 70.0f * (n0 + n1 + n2);
  }

  public double noise(double x, double y, double z) {
    final double F3 = 1.0 / 3.0;
    final double G3 = 1.0 / 6.0;

    double s = (x + y + z) * F3;
    int i = fastFloor(x + s);
    int j = fastFloor(y + s);
    int k = fastFloor(z + s);

    double t = (i + j + k) * G3;
    double X0 = i - t;
    double Y0 = j - t;
    double Z0 = k - t;
    double x0 = x - X0;
    double y0 = y - Y0;
    double z0 = z - Z0;

    int i1, j1, k1;
    int i2, j2, k2;

    if(x0 >= y0) {
      if(y0 >= z0) {
        i1 = 1;
        j1 = 0;
        k1 = 0;
        i2 = 1;
        j2 = 1;
        k2 = 0;
      } else if(x0 >= z0) {
        i1 = 1;
        j1 = 0;
        k1 = 0;
        i2 = 1;
        j2 = 0;
        k2 = 1;
      } else {
        i1 = 0;
        j1 = 0;
        k1 = 1;
        i2 = 1;
        j2 = 0;
        k2 = 1;
      }
    } else {
      if(y0 < z0) {
        i1 = 0;
        j1 = 0;
        k1 = 1;
        i2 = 0;
        j2 = 1;
        k2 = 1;
      } else if(x0 < z0) {
        i1 = 0;
        j1 = 1;
        k1 = 0;
        i2 = 0;
        j2 = 1;
        k2 = 1;
      } else {
        i1 = 0;
        j1 = 1;
        k1 = 0;
        i2 = 1;
        j2 = 1;
        k2 = 0;
      }
    }

    double x1 = x0 - i1 + G3;
    double y1 = y0 - j1 + G3;
    double z1 = z0 - k1 + G3;
    double x2 = x0 - i2 + 2 * G3;
    double y2 = y0 - j2 + 2 * G3;
    double z2 = z0 - k2 + 2 * G3;
    double x3 = x0 - 1 + 3 * G3;
    double y3 = y0 - 1 + 3 * G3;
    double z3 = z0 - 1 + 3 * G3;

    int ii = i & 255;
    int jj = j & 255;
    int kk = k & 255;

    int gi0 = perm[(ii + perm[(jj + perm[kk]) & 511]) & 511] % 12;
    int gi1 = perm[(ii + i1 + perm[(jj + j1 + perm[(kk + k1) & 511]) & 511]) & 511] % 12;
    int gi2 = perm[(ii + i2 + perm[(jj + j2 + perm[(kk + k2) & 511]) & 511]) & 511] % 12;
    int gi3 = perm[(ii + 1 + perm[(jj + 1 + perm[(kk + 1) & 511]) & 511]) & 511] % 12;

    double n0 = 0, n1 = 0, n2 = 0, n3 = 0;
    double t0 = 0.6 - x0 * x0 - y0 * y0 - z0 * z0;
    if(t0 >= 0) {
      t0 *= t0;
      n0 = t0 * t0 * dot(grad3[gi0], x0, y0, z0);
    }
    double t1 = 0.6 - x1 * x1 - y1 * y1 - z1 * z1;
    if(t1 >= 0) {
      t1 *= t1;
      n1 = t1 * t1 * dot(grad3[gi1], x1, y1, z1);
    }
    double t2 = 0.6 - x2 * x2 - y2 * y2 - z2 * z2;
    if(t2 >= 0) {
      t2 *= t2;
      n2 = t2 * t2 * dot(grad3[gi2], x2, y2, z2);
    }
    double t3 = 0.6 - x3 * x3 - y3 * y3 - z3 * z3;
    if(t3 >= 0) {
      t3 *= t3;
      n3 = t3 * t3 * dot(grad3[gi3], x3, y3, z3);
    }
    return 32.0 * (n0 + n1 + n2 + n3);
  }

  private double dot(int[] g, double x, double y) {
    return g[0] * x + g[1] * y;
  }

  private double dot(int[] g, double x, double y, double z) {
    return g[0] * x + g[1] * y + g[2] * z;
  }

  private int fastFloor(double x) {
    int xi = (int)x;
    return x < xi ? xi - 1 : xi;
  }
}
