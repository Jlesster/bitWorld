package com.jless.voxelGame.render;

public class TextureAtlas {

  private final int tilesX;
  private final int tilesY;
  private final float tileW;
  private final float tileH;

  public TextureAtlas(int atlasWpx, int atlasHpx, int tileSizePX) {
    this.tilesX = atlasWpx / tileSizePX;
    this.tilesY = atlasHpx / tileSizePX;

    this.tileW = 1.0f / tilesX;
    this.tileH = 1.0f / tilesY;
  }

  public static int tile(int x, int y) {
    return (x & 0xFFFF) | ((y & 0xFFFF) << 16);
  }

  public static int tileX(int packed) {
    return packed & 0xFFFF;
  }

  public static int tileY(int packed) {
    return (packed >>> 16) & 0xFFFF;
  }

  public static class UVRect {
    public final float u0, v0, u1, v1;

    public UVRect(float u0, float v0, float u1, float v1) {
      this.u0 = u0;
      this.v0 = v0;
      this.u1 = u1;
      this.v1 = v1;
    }
  }

  public int tilesX() { return tilesX; }
  public int tilesY() { return tilesY; }

  public UVRect getUVRect(int packedTile) {
    int tx = tileX(packedTile);
    int ty = tileY(packedTile);

    float u0 = tx * tileW;
    float v0 = ty * tileH;

    float u1 = u0 + tileW;
    float v1 = v0 + tileH;

    return new UVRect(u0, v0, u1, v1);
  }
}
