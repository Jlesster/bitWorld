package com.jless.voxelGame;

public class Time {

  private static double lastTime;
  private static float delta;

  public static void init() {
    lastTime = now();
    delta = 0.0f;
  }

  public static void update() {
    double current = now();
    delta = (float)(current - lastTime);
    lastTime = current;
  }

  public static float dt() {
    return delta;
  }

  private static double now() {
    return System.nanoTime() / 1_000_000_000.0;
  }

  private Time() {}
}
