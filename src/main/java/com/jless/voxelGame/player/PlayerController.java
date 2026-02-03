package com.jless.voxelGame.player;

import static org.lwjgl.glfw.GLFW.*;

import java.lang.Math;

import org.joml.*;

import com.jless.voxelGame.*;
import com.jless.voxelGame.blocks.*;
import com.jless.voxelGame.worldGen.*;

public class PlayerController {

  public Vector3f velocity = new Vector3f();
  public static Vector3f pos;
  private Matrix4f viewMat;

  private int lastCX = Integer.MIN_VALUE;
  private int lastCZ = Integer.MIN_VALUE;

  public static float pitch;
  public static float yaw;
  private float roll;

  private float moveSpeed = 0.8f;
  private boolean onGround = false;
  private boolean wishMoving = false;

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

    float eyeHeight = Consts.EYE_HEIGHT;

    viewMat.translate(-pos.x, -(pos.y + eyeHeight), -pos.z);

    return viewMat;
  }

  public void processMouse(float dx, float dy, float dt) {
    float timeFactor = dt * 60.0f;
    yaw += (float)Math.toRadians(dx * Consts.MOUSE_SENS * timeFactor);
    pitch += (float)Math.toRadians(dy * Consts.MOUSE_SENS * timeFactor);

    if(pitch > Math.toRadians(89.0f)) pitch = (float)Math.toRadians(89.0f);
    if(pitch < Math.toRadians(-89.0f)) pitch = (float)Math.toRadians(-89.0f);
  }

  private void handleInput(float dt) {
    wishMoving = false;
    Vector3f wishDir = new Vector3f();

    float cosYaw = (float)Math.cos(yaw);
    float sinYaw = (float)Math.sin(yaw);

    Vector3f forward = new Vector3f(sinYaw, 0, -cosYaw);
    Vector3f right = new Vector3f(cosYaw, 0, sinYaw);

    if(Input.isKeyPressed(GLFW_KEY_W)) {
      wishDir.add(forward);
      wishMoving = true;
    }
    if(Input.isKeyPressed(GLFW_KEY_S)) {
      wishDir.sub(forward) ;
      wishMoving = true;
    }

    if(Input.isKeyPressed(GLFW_KEY_D)) {
      wishDir.add(right);
      wishMoving = true;
    }
    if(Input.isKeyPressed(GLFW_KEY_A)) {
      wishDir.sub(right);
      wishMoving = true;
    }

    if(wishDir.lengthSquared() > 0) {
      wishDir.normalize();
      float currentSpeed = onGround ? Consts.GROUND_ACCEL * dt : Consts.AIR_ACCEL * dt;
      float maxSpeed = onGround ? Consts.MAX_GROUND_SPEED : Consts.MAX_AIR_SPEED;
      float accel = onGround ? Consts.GROUND_ACCEL : Consts.AIR_ACCEL;

      Vector3f horizVel = new Vector3f(velocity.x, 0, velocity.z);

      Vector3f targetVel = new Vector3f(wishDir).mul(maxSpeed);

      horizVel.lerp(targetVel, accel * dt);

      velocity.x = horizVel.x * moveSpeed;
      velocity.z = horizVel.z * moveSpeed;
    }
  }

  private void applyPhysics(World world, float dt, boolean jumpPressed) {
    velocity.y += Consts.GRAVITY * dt;

    if(jumpPressed && onGround) {
      velocity.y = Consts.JUMP_VEL;
      onGround = false;
    }

    if(onGround && !wishMoving) {
      velocity.x *= 0.8f;
      velocity.z *= 0.8f;
    }

    moveAndCollide(world, dt);
  }

  private void moveAndCollide(World world, float dt) {
    Vector3f stepPos = new Vector3f(pos);
    boolean collidedY = false;

    stepPos.x += velocity.x * dt;
    if(checkAABBCollision(world, stepPos)) {
      stepPos.x = pos.x;
      velocity.x = 0;
    }

    stepPos.y += velocity.y * dt;
    if(checkAABBCollision(world, stepPos)) {
      stepPos.y = pos.y;
      if(velocity.y < 0) onGround = true;
      velocity.y = 0;
      collidedY = true;
      stepPos.y = (float)Math.floor(stepPos.y + 0.001f);
    }

    stepPos.z += velocity.z * dt;
    if(checkAABBCollision(world, stepPos)) {
      stepPos.z = pos.z;
      velocity.z = 0;
    }

    if(!collidedY && velocity.y != 0) {
      onGround = false;
    }

    pos.set(stepPos);
  }

  private boolean checkAABBCollision(World w, Vector3f testPos) {
    float hw = Consts.PLAYER_WIDTH / 2.0f;
    float height = Consts.PLAYER_HEIGHT;

    float minX = testPos.x - hw;
    float maxX = testPos.x + hw;
    float minY = testPos.y;
    float maxY = testPos.y + height;
    float minZ = testPos.z - hw;
    float maxZ = testPos.z + hw;

    int blockMinX = (int)Math.floor(minX);
    int blockMaxX = (int)Math.floor(maxX);
    int blockMinY = (int)Math.floor(minY);
    int blockMaxY = (int)Math.floor(maxY);
    int blockMinZ = (int)Math.floor(minZ);
    int blockMaxZ = (int)Math.floor(maxZ);

    for(int bx = blockMinX; bx <= blockMaxX; bx++) {
      for(int by = blockMinY; by <= blockMaxY; by++) {
        for(int bz = blockMinZ; bz <= blockMaxZ; bz++) {
          byte blockID = w.getIfLoaded(bx, by, bz);

          if(Blocks.SOLID[blockID]) {
            if(aabbIntersects(minX, minY, minZ, maxX, maxY, maxZ,
                              bx, by, bz, bx + 1, by + 1, bz + 1)) {
              return true;
            }
          }
        }
      }
    }
    return false;
  }

  private boolean aabbIntersects(float min1X, float min1Y, float min1Z,
                                 float max1X, float max1Y, float max1Z,
                                 float min2X, float min2Y, float min2Z,
                                 float max2X, float max2Y, float max2Z
  ) {
    return (min1X < max2X && max1X > min2X) &&
           (min1Y < max2Y && max1Y > min2Y) &&
           (min1Z < max2Z && max1Z > min2Z);
  }

  public static boolean wouldCollideWithBlock(int bx, int by, int bz) {
    float minX = pos.x - Consts.PLAYER_WIDTH / 2f;
    float minY = pos.y;
    float minZ = pos.z - Consts.PLAYER_WIDTH / 2f;
    float maxX = pos.x + Consts.PLAYER_WIDTH / 2f;
    float maxY = pos.y + Consts.PLAYER_HEIGHT;
    float maxZ = pos.z + Consts.PLAYER_WIDTH / 2f;

    return bx < maxX && bx + 1 > minX &&
           by < maxY && by + 1 > minY &&
           bz < maxZ && bz + 1 > minZ;
  }

  private boolean checkCollision(World world, Vector3f testPos) {
    float hW = Consts.PLAYER_WIDTH / 2;
    float playerH = Consts.PLAYER_HEIGHT;

    for(int x = 0; x < 2; x++) {
      for(int y = 0; y < 2; y++) {
        for(int z = 0; z < 2; z++) {
          float checkX = testPos.x + (x == 0 ? -hW : hW);
          float checkY = testPos.y + (y == 0 ? 0.01f : playerH);
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

  private void updateCameraRotation(float dt) {
    float mouseDX = Input.getMouseDX();
    float mouseDY = Input.getMouseDY();

    processMouse(mouseDX, mouseDY, dt);
  }

  public void update(World world, float dt, boolean jumpPressed, ChunkThreadManager threadManager, boolean allowStreaming) {
    int playerCX = (int)Math.floor(PlayerController.pos.x / Consts.CHUNK_SIZE);
    int playerCZ = (int)Math.floor(PlayerController.pos.z / Consts.CHUNK_SIZE);

    if(playerCX != lastCX || playerCZ != lastCZ && allowStreaming) {
      world.updateStreaming(playerCX, playerCZ, threadManager);
      lastCX = playerCX;
      lastCZ = playerCZ;
    }

    updateCameraRotation(dt);
    handleInput(dt);
    applyPhysics(world, dt, jumpPressed);
  }

  public static Vector3f getEyePos() {
    return new Vector3f(
      pos.x,
      pos.y + Consts.EYE_HEIGHT,
      pos.z
    );
  }

  public static Vector3f getForwardDir() {
    float cosYaw = (float)Math.cos(yaw);
    float sinYaw = (float)Math.sin(yaw);
    float sinPitch = (float)Math.sin(pitch);
    float cosPitch = (float)Math.cos(pitch);
    return new Vector3f(sinYaw * cosPitch, -sinPitch, -cosYaw * cosPitch).normalize();
  }

  public Vector3f getRightDir() {
    float cosYaw = (float)Math.cos(yaw);
    float sinYaw = (float)Math.sin(yaw);
    return new Vector3f(cosYaw, 0, -sinYaw).normalize();
  }
}
