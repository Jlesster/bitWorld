package com.jless.voxelGame.player;

import org.lwjgl.glfw.GLFW;

import org.joml.Vector3f;

import com.jless.voxelGame.Input;

public class PlayerController {

  private final Player player;

  public float sens = 0.12f;
  public float flySpeed = 10.0f;
  public float sprintMult = 3.0f;

  public PlayerController(Player player) {
    this.player = player;
  }

  public void update(float dt) {
    System.out.println("dx= " + Input.mouseDX() + " dy= " + Input.mouseDY());
    if(Input.pressed(GLFW.GLFW_KEY_ESCAPE)) {
      Input.captureMouse(false);
    }

    if(!Input.isCaptured()) return;

    float dx = Input.mouseDX();
    float dy = Input.mouseDY();

    player.yaw += dx * sens;
    player.pitch -= dy * sens;

    if(player.pitch > 89.9f) player.pitch = 89.9f;
    if(player.pitch < -89.9f) player.pitch = -89.9f;

    float speed = flySpeed;
    if(Input.down(GLFW.GLFW_KEY_LEFT_CONTROL)) {
      speed *= sprintMult;
    }

    Vector3f move = new Vector3f();
    float yawRad = (float)Math.toRadians(player.yaw);

    Vector3f forward = new Vector3f(
      (float)Math.cos(yawRad),
      0,
      (float)Math.sin(yawRad)
    ).normalize();

    Vector3f right = new Vector3f(forward.z, 0, -forward.x).normalize();

    if(Input.down(GLFW.GLFW_KEY_W)) move.add(forward);
    if(Input.down(GLFW.GLFW_KEY_S)) move.sub(forward);
    if(Input.down(GLFW.GLFW_KEY_A)) move.add(right);
    if(Input.down(GLFW.GLFW_KEY_D)) move.sub(right);

    if(Input.down(GLFW.GLFW_KEY_SPACE)) move.y += 1.0f;
    if(Input.down(GLFW.GLFW_KEY_LEFT_SHIFT)) move.y -= 1.0f;

    if(move.lengthSquared() > 0) {
      move.normalize().mul(speed * dt);
      player.position.add(move);
    }
  }
}
