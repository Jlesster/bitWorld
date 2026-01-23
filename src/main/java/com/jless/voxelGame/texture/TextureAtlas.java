package com.jless.voxelGame.texture;

public final class TextureAtlas {

  public static final int TILE_SIZE_PX = 16;
  public static final int ATLAS_TILE_X = 24;
  public static final int ATLAS_TILE_Y = 12;

  public static int tile(int tx, int ty) {
    return (ty << 8) | tx;
  }

  public static int tileX(int packed) { return packed & 0xFF; }
  public static int tileY(int packed) { return (packed >> 8) & 0xFF; }
}
