package com.jless.voxelGame.player;

import java.lang.Math;
import java.nio.*;

import org.lwjgl.*;

import org.joml.*;

import com.jless.voxelGame.blocks.*;
import com.jless.voxelGame.entity.*;
import com.jless.voxelGame.texture.*;

public class PlayerHand {

  private float swingProgress = 0f;
  private float swingSpeed = 4.0f;
  private boolean isSwinging = false;

  private float bobTime = 0f;

  private static final float HAND_X = 0.6f;
  private static final float HAND_Y = -0.4f;
  private static final float HAND_Z = -0.6f;

  private static final float ARM_WIDTH = 0.25f;
  private static final float ARM_HEIGHT = 0.4f;
  private static final float ARM_LENGTH = 0.1f;
  private static final float HAND_SIZE = 0.2f;

  private static final float[] SKIN_COLOR = {0.95f, 0.8f, 0.7f};
  private static final float[] SKIN_DARK = {0.85f, 0.7f, 0.6f};

  private final FloatBuffer identityViewMatrix = BufferUtils.createFloatBuffer(16);
  private final Matrix4f identityMat = new Matrix4f();

  public PlayerHand() {
    identityMat.identity().get(identityViewMatrix);
  }

  public void startSwinging() {
    if(!isSwinging) {
      isSwinging = true;
      swingProgress = 0f;
    }
  }

  public void update(float dt, Vector3f velocity) {
    if(isSwinging) {
      swingProgress += swingSpeed * dt;
      if(swingProgress >= 1.0f) {
        swingProgress = 0f;
        isSwinging = false;
      }
    }

    float moveSq = velocity.x * velocity.x + velocity.z * velocity.z;
    if(moveSq > 0.01f) {
      bobTime += dt * 8.0f;
    } else {
      bobTime = 0f;
    }
  }

  public void render(byte heldBlock) {
    Shaders.setViewMatrix(identityViewMatrix);
    Matrix4f handTransform = new Matrix4f();

    handTransform.translate(HAND_X, HAND_Y, HAND_Z);

    if(isSwinging) {
      float swingAngle = calculateSwingAngle();
      handTransform.rotateX(swingAngle);

      float pivotY = -ARM_HEIGHT;
      handTransform.translate(0f, pivotY, 0f);
      handTransform.rotateX(swingAngle);
      handTransform.translate(0f, -pivotY, 0f);
    }

    float moveSq = PlayerController.pos != null ? PlayerController.pos.lengthSquared() : 0f;
    if(bobTime > 0.01f) {
      float bobOffset = (float)Math.sin(bobTime) * 0.03f;
      float bobRotation = (float)Math.sin(bobTime * 0.05f) * 0.1f;
      handTransform.translate(0f, bobOffset, 0f);
      handTransform.rotateZ(bobRotation);
    }
    renderArm(handTransform);
    renderHand(handTransform);

    if(heldBlock != BlockID.AIR) {
      renderHeldBlock(handTransform, heldBlock);
    }
  }

  private void renderArm(Matrix4f transform) {
    transform.rotateX((float)Math.toRadians(-25));
    EntityRender.drawEntityWithTransform(
      transform,
      -ARM_WIDTH / 2, -ARM_HEIGHT, -ARM_LENGTH / 2,
      ARM_WIDTH / 2, 0f, ARM_LENGTH / 2,
      SKIN_COLOR
    );
  }

  private void renderHand(Matrix4f transform) {
    EntityRender.drawEntityWithTransform(
      transform,
      -HAND_SIZE * 0.7f, -0.01f, -HAND_SIZE * 0.3f,
      HAND_SIZE * 0.7f, HAND_SIZE * 0.5f, HAND_SIZE * 0.3f,
      SKIN_DARK
    );

    EntityRender.drawEntityWithTransform(
      transform,
      HAND_SIZE / 2, 0f, -HAND_SIZE * 0.3f,
      HAND_SIZE / 2 + HAND_SIZE * 0.4f, HAND_SIZE * 0.2f, HAND_SIZE * 0.1f,
      SKIN_COLOR
    );
  }

  private void renderHeldBlock(Matrix4f transform, byte blockID) {
    Matrix4f blockTransform = new Matrix4f(transform);
    blockTransform.translate(0.08f, 0.15f, -0.08f);
    blockTransform.scale(0.2f);

    blockTransform.rotateY((float)Math.toRadians(-45));
    blockTransform.rotateX((float)Math.toRadians(15));

    int id = blockID & 0xFF;
    int tile = Blocks.TEX_SIDE[id];

    EntityRender.drawTexturedEntityWithTransform(
      blockTransform,
      -0.5f, -0.5f, -0.5f,
      0.5f, 0.5f, 0.5f,
      tile
    );
  }

  private float calculateSwingAngle() {
    float t = swingProgress;
    float eased = 1f - (1f - t) * (1f - t);

    return (float)Math.sin(eased * Math.PI) * (float)Math.toRadians(-60);
  }

}
