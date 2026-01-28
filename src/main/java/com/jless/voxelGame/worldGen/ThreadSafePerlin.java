package com.jless.voxelGame.worldGen;

import com.jless.voxelGame.*;

public class ThreadSafePerlin {

  private static final ThreadLocal<Perlin> threadLocalNoise = ThreadLocal.withInitial(() -> new Perlin(Consts.SEED));

  public static float noise2D(float x, float y) {
    return threadLocalNoise.get().noise2D(x, y);
  }

  public static float noise3D(float x, float y, float z) {
    return threadLocalNoise.get().noise(x, y, z);
  }

  public static void cleanup() {
    threadLocalNoise.remove();
  }
}
