package com.jless.voxelGame.player;

import org.joml.*;

public class RaycastHit {
  public final Vector3i block;
  public final Vector3i normal;

  public RaycastHit(Vector3i block, Vector3i normal) {
    this.block = block;
    this.normal = normal;
  }
}
