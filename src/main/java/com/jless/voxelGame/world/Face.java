package com.jless.voxelGame.world;

public enum Face {
  NORTH(0, 0, -1),
  SOUTH(0, 0, 1),
  EAST(1, 0, 0),
  WEST(-1, 0, 0),
  UP(0, 1, 0),
  DOWN(0, -1, 0);

  public final int dx, dy, dz;

  Face(int dx, int dy, int dz) {
    this.dx = dx;
    this.dy = dy;
    this.dz = dz;
  }
}
