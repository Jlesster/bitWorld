package com.jless.voxelGame.player;

import java.lang.Math;

import org.joml.*;

import com.jless.voxelGame.*;

public class PlayerController {

  private final Vector3f velocity = new Vector3f();
  private Vector3f pos;
  private Matrix4f viewMat;

  private float pitch;
  private float yaw;
  private float roll;

  private float moveSpeed = 0.1f;
  private boolean onGround = false;

  public PlayerController(float x, float y, float z) {
    pos = new Vector3f(x, y, z);
    pitch = 0;
    yaw = 0;
    roll = 0;
    viewMat = new Matrix4f();
  }

  public Matrix4f getViewMatrix() {
    viewMat.identity();

    viewMat.rotateX(pitch);
    viewMat.rotateY(yaw);
    viewMat.rotateZ(roll);

    viewMat.translate(-pos.x, -pos.y, -pos.z);

    return viewMat;
  }

  public void processMosue(float dx, float dy) {
    yaw += (float)Math.toRadians(dx * Consts.MOUSE_SENS);
    pitch += (float)Math.toRadians(dy * Consts.MOUSE_SENS);

    if(pitch > Math.toRadians(89.0f)) pitch = (float)Math.toRadians(89.0f);
    if(pitch < Math.toRadians(-89.0f)) pitch = (float)Math.toRadians(-89.0f);
  }

  public void update() {
    //TODO add update in passing world, delta time and jump boolean
  }
}
