package com.jless.voxelGame;

public class LightingConsts {

  public static final float DAY_LENGTH = 1200.0f;
  public static final float START_TIME = 0.25f;
  public static final float AMBIENT_NIGHT = 0.15f;
  public static final float AMBIENT_DAY = 0.35f;

  public static final float SUN_TILT = 0.3f;
  public static final float SUN_MAX_BRIGHTNESS = 1.0f;
  public static final float MOON_MAX_BRIGHTNESS = 0.3f;

  public static final float[] SKY_DAY = {0.5f, 0.7f, 1.0f};
  public static final float[] SKY_SUNSET = {0.8f, 0.4f, 0.2f};
  public static final float[] SKY_NIGHT = {0.02f, 0.02f, 0.05f};

  public static final float[] SUN_COLOR = {1.0f, 0.95f, 0.8f};
  public static final float[] MOON_COLOR = {0.4f, 0.5f, 0.8f};

  public static final boolean ENABLE_SHADOWS = true;
  public static final int SHADOW_MAP_SIZE = 2048;

  public static final float SHADOW_DISTANCE = 128.0f;
  public static final float SHADOW_NEAR = -100.0f;
  public static final float SHADOW_FAR = 200.0f;
  public static final float SHADOW_BIAS = 0.005f;
}
