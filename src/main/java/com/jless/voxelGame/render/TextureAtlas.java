package com.jless.voxelGame.render;

public class TextureAtlas {

  private final Texture tex;

  private final int tileSize;
  private final int tilesX;
  private final int tilesY;

  public TextureAtlas(Texture texture, int tileSizePX) {
    this.tex = texture;
    this.tileSize = tileSizePX;

    if(tex.width() % tileSize != 0 || tex.height() % tileSize != 0) {
      throw new IllegalArgumentException("Texture atlas: sheet must be divisible by tile size");
    }

    this.tilesX = tex.width() / tileSize;
    this.tilesY = tex.height() / tileSize;
  }

  public Texture texture() {
    return tex;
  }

  public int tilesX() { return tilesX; }
  public int tilesY() { return tilesY; }

  public float[] getUVRect(int tileX, int tileY) {
    if(tileX < 0 || tileX >= tilesX || tileY < 0 || tileY >= tilesY) {
      throw new IllegalArgumentException("Tile out of range");
    }

    float u0 = (tileX * tileSize) / (float)tex.width();
    float v0 = (tileY * tileSize) / (float)tex.height();

    float u1 = ((tileX + 1) * tileSize) / (float)tex.width();
    float v1 = ((tileY + 1) * tileSize) / (float)tex.height();

    return new float[] { u0, v0, u1, v1 };
  }
}
