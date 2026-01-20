package com.jless.voxelGame.world;

import com.jless.voxelGame.Consts;
import com.jless.voxelGame.render.Mesh;

public class Chunk {

  public Mesh mesh;

  private final byte[] blocks;
  public final int cx;
  public final int cz;

  private boolean dirty = true;

  public Chunk(int cx, int cz) {
    this.cx = cx;
    this.cz = cz;

    blocks = new byte[Consts.CHUNK_X * Consts.CHUNK_Y * Consts.CHUNK_Z];
  }

  private int index(int x, int y, int z) {
    return x + Consts.CHUNK_X * (z + Consts.CHUNK_Z * y);
  }

  public boolean inBounds(int x, int y, int z) {
    return x >= 0 && x < Consts.CHUNK_X
        && y >= 0 && y < Consts.CHUNK_Y
        && z >= 0 && z < Consts.CHUNK_Z;
  }

  public byte getLocal(int x, int y, int z) {
    if(!inBounds(x, y, z)) return BlockID.AIR;
    return blocks[index(x, y, z)];
  }

  public void setLocal(int x, int y, int z, byte id) {
    if(!inBounds(x, y, z)) return;

    int idx = index(x, y, z);
    blocks[idx] = id;
    dirty = true;
  }

  public boolean isDirty() {
    return dirty;
  }

  public void clearDirty() {
    dirty = false;
  }
}
