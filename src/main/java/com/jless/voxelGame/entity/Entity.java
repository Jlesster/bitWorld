package com.jless.voxelGame.entity;

import org.joml.*;

import com.jless.voxelGame.worldGen.*;

public abstract class Entity {

  public final Vector3f pos = new Vector3f();
  public final Vector3f vel = new Vector3f();

  public boolean removed = false;

  public abstract void update(World world, float dt);
  public abstract void render(Rendering render);

  public static Matrix4f withPivotRotationX(Matrix4f base, float px, float py, float pz, float angleRad) {
    return new Matrix4f(base).translate(px, py, pz).rotateX(angleRad).translate(-px, -py, -pz);
  }
}
