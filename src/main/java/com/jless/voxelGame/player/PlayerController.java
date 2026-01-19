package com.jless.voxelGame.player;

import org.lwjgl.glfw.GLFW;

import com.jless.voxelGame.Input;

public class PlayerController {

  private final Player player;

  public float sens = 0.12f;

  public PlayerController(Player player) {
    this.player = player;
  }

  public void update(float dt) {
    if(Input.pressed(GLFW.GLFW_KEY_ESCAPE)) {
      Input.captureMouse(false);
    }

    if(!Input.isCaptured()) return;

    float dx = Input.mouseDX();
    float dy = Input.mouseDY();

    player.yaw += dx * sens;
    player.pitch += dy * sens;

    if(player.pitch > 89.9f) player.pitch = 89.9f;
    if(player.pitch < -89.9f) player.pitch = -89.9f;
  }
}
