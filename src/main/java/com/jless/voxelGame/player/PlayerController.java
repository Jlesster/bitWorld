package com.jless.voxelGame.player;

import static org.lwjgl.glfw.GLFW.*;

import java.lang.Math;

import org.joml.*;

import com.jless.voxelGame.*;
import com.jless.voxelGame.blocks.*;
import com.jless.voxelGame.worldGen.*;

public class PlayerController {

  private Vector3f velocity = new Vector3f();
  public Vector3f pos;
  private Matrix4f viewMat;

  private float pitch;
  private float yaw;
  private float roll;

  private float moveSpeed = 0.1f;
  private boolean onGround = false;

  public PlayerController(float x, float y, float z) {
    pos = new Vector3f(x, y, z);
    velocity = new Vector3f(0, 0, 0);
    pitch = 0;
    yaw = 0;
    roll = 0;
    viewMat = new Matrix4f();
    onGround = false;
  }

  public Matrix4f getViewMatrix() {
    viewMat.identity();

    viewMat.rotateX(pitch);
    viewMat.rotateY(yaw);
    viewMat.rotateZ(roll);

    viewMat.translate(-pos.x, -pos.y, -pos.z);

    return viewMat;
  }

  public void processMouse(float dx, float dy) {
    yaw += (float)Math.toRadians(dx * Consts.MOUSE_SENS);
    pitch += (float)Math.toRadians(dy * Consts.MOUSE_SENS);

    if(pitch > Math.toRadians(89.0f)) pitch = (float)Math.toRadians(89.0f);
    if(pitch < Math.toRadians(-89.0f)) pitch = (float)Math.toRadians(-89.0f);
  }

  private void handleInput(float dt) {
    Vector3f moveDir = new Vector3f(0, 0, 0);

    float cosYaw = (float)Math.cos(yaw);
    float sinYaw = (float)Math.sin(yaw);

    Vector3f forward = new Vector3f(-sinYaw, 0, -cosYaw).normalize();
    Vector3f right = new Vector3f(cosYaw, 0, -sinYaw).normalize();

    if(Input.isKeyPressed(GLFW_KEY_W)) moveDir.add(forward);
    if(Input.isKeyPressed(GLFW_KEY_S)) moveDir.sub(forward);

    if(Input.isKeyPressed(GLFW_KEY_D)) moveDir.add(right);
    if(Input.isKeyPressed(GLFW_KEY_A)) moveDir.sub(right);

    if(moveDir.lengthSquared() > 0) {
      moveDir.normalize();
      float currentSpeed = onGround ? Consts.GROUND_ACCEL * dt : Consts.AIR_ACCEL * dt;
      velocity.x += moveDir.x * currentSpeed;
      velocity.z += moveDir.z * currentSpeed;
    }
  }

  private void applyPhysics(World world, float dt, boolean jumpPressed) {
    velocity.y += Consts.GRAVITY * dt;

    if(jumpPressed && onGround) {
      velocity.y = Consts.JUMP_VEL;
      onGround = false;
    }

    float friction = onGround ? Consts.GROUND_FRICTION * dt : 0;
    velocity.x *= (1 - friction);
    velocity.z *= (1 - friction);

    float maxSpeed = onGround ? Consts.MAX_GROUND_SPEED : Consts.MAX_AIR_SPEED;
    float horizontalSpeed = (float)Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z);

    if(horizontalSpeed > maxSpeed) {
      float scale = maxSpeed / horizontalSpeed;
      velocity.x *= scale;
      velocity.z *= scale;
    }

    moveAndCollide(world, dt);
  }

  private void moveAndCollide(World world, float dt) {
    Vector3f stepPos = new Vector3f(pos);

    stepPos.x += velocity.x * dt;
    if(checkCollision(world, stepPos)) {
      stepPos.x = pos.x;
      velocity.x = 0;
    }

    stepPos.y += velocity.y * dt;
    if(checkCollision(world, stepPos)) {
      stepPos.y = pos.y;
      if(velocity.y < 0) onGround = true;
      velocity.y = 0;
    } else {
      onGround = false;
    }

    stepPos.z += velocity.z * dt;
    if(checkCollision(world, stepPos)) {
      stepPos.z = pos.z;
      velocity.z = 0;
    }
    pos.set(stepPos);
  }

  private boolean checkCollision(World world, Vector3f testPos) {
    float hW = Consts.PLAYER_WIDTH / 2;
    float playerH = Consts.PLAYER_HEIGHT;

    for(int x = 0; x < 2; x++) {
      for(int y = 0; y < 2; y++) {
        for(int z = 0; z < 2; z++) {
          float checkX = testPos.x + (x == 0 ? -hW : hW);
          float checkY = testPos.y + (y == 0 ? Consts.EYE_HEIGHT : playerH - Consts.EYE_HEIGHT);
          float checkZ = testPos.z + (z == 0 ? -hW : hW);

          int blockX = (int)Math.floor(checkX);
          int blockY = (int)Math.floor(checkY);
          int blockZ = (int)Math.floor(checkZ);

          byte blockID = world.getIfLoaded(blockX, blockY, blockZ);

          if(Blocks.SOLID[blockID]) {
            return true;
          }
        }
      }
    }
    return false;
  }

  private void updateCameraRotation() {
    float mouseDX = Input.getMouseDX();
    float mouseDY = Input.getMouseDY();

    processMouse(mouseDX, mouseDY);
  }

  public void update(World world, float dt, boolean jumpPressed) {
    handleInput(dt);
    applyPhysics(world, dt, jumpPressed);
    updateCameraRotation();
  }
}
