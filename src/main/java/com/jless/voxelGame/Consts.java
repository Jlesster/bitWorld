package com.jless.voxelGame;

public final class Consts {

  //window
  public static final int W_WIDTH = 1280;
  public static final int W_HEIGHT = 720;
  public static final String W_TITLE = "BitWorld";

  public static final int VSYNC = 1;
  public static final float FOV = 90.0f;
  public static final int THREAD_COUNT = Runtime.getRuntime().availableProcessors() - 1;

  public static final int MAX_RETRIES = 3;
  public static final long[] RETRY_DELAYS = {
    1000, 2000, 4000
  };

  public static final int CHUNK_SIZE = 16;
  public static final int WORLD_HEIGHT = 128;
  public static final int INIT_CHUNK_RADS = 12;
  public static final boolean ENABLE_GREEDY_MESHING = false;

  public static final long SEED = 1337L; //TODO rng static method in world gen

  public static final int PERLIN_SIZE = 2048;
  public static final int PERLIN_MASK = 2047;

  public static final float EYE_HEIGHT = 1.4f;
  public static final float PLAYER_WIDTH = 0.6f;
  public static final float PLAYER_HEIGHT = 1.8f;

  public static final float MAX_GROUND_SPEED = 6.0f;
  public static final float MAX_AIR_SPEED = 4.5f;
  public static final float GROUND_ACCEL = 40.0f;
  public static final float AIR_ACCEL = 15.0f;
  public static final float GROUND_FRICTION = 20.0f;
  public static final float GRAVITY = -30.0f;
  public static final float JUMP_VEL = 9.0f;

  public static final float MOUSE_SENS = 0.15f;

  private Consts() {}
}
