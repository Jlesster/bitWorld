package com.jless.voxelGame.world;

import java.util.Objects;

public class ChunkPos {

  public final int cx;
  public final int cz;

  public ChunkPos(int cx, int cz) {
    this.cx = cx;
    this.cz = cz;
  }

  @Override
  public boolean equals(Object o) {
    if(this == o) return true;
    if(!(o instanceof ChunkPos)) return false;
    ChunkPos other = (ChunkPos)o;
    return cx == other.cx && cz == other.cz;
  }

  @Override
  public int hashCode() {
    return Objects.hash(cx, cz);
  }

  @Override
  public String toString() {
    return "ChunkPos(" + cx + ", " + cz + ")";
  }
}
