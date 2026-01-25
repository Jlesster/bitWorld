package com.jless.voxelGame;

import static org.lwjgl.glfw.GLFW.*;

import java.lang.Math;
import java.nio.*;

import org.lwjgl.*;

import org.joml.*;

import com.jless.voxelGame.player.*;
import com.jless.voxelGame.texture.*;
import com.jless.voxelGame.worldGen.*;

public class App {

  private World world;
  private PlayerController player;
  private FloatBuffer projMatrix;
  private FloatBuffer viewMatrix;

  private void init() {
    Window.create(Consts.W_WIDTH, Consts.W_HEIGHT, Consts.W_TITLE);
    Shaders.create();
    Rendering.create();

    world = new World();
    player = new PlayerController(0, Consts.WORLD_HEIGHT * 2, 0);

    world.generateSpawn();
    setupMatrices();
    Input.setup(Window.getWindow());
  }

  private void setupMatrices() {
    float aspect = (float)Consts.W_WIDTH / Consts.W_HEIGHT;
    Matrix4f proj = new Matrix4f().perspective((float)Math.toRadians(Consts.FOV), aspect, 0.1f, 500.0f);
    projMatrix = BufferUtils.createFloatBuffer(16);
    proj.get(projMatrix);
    viewMatrix = BufferUtils.createFloatBuffer(16);
  }

public void run() {
    waylandCheck();
    init();

    loop();

    cleanup();
  }

  private void loop() {
    while(!glfwWindowShouldClose(Window.getWindow())) {
      Input.update();

      boolean jumpPressed = Input.isKeyPressed(GLFW_KEY_SPACE);
      player.update(world, 0.016f, jumpPressed);
      player.getViewMatrix().get(viewMatrix);

      Shaders.use();
      Shaders.setViewMatrix(viewMatrix);
      Shaders.setProjMatrix(projMatrix);
      Rendering.beginFrame();

      Vector3f playerPos = player.pos;
      Rendering.renderWorld(world, playerPos);
      Rendering.endFrame();
      Window.update();
    }
  }

  private void cleanup() {
    Window.destroy();
    Rendering.cleanup();
    Shaders.cleanup();
  }

  public static void main(String[] args) {
    new App().run();
  }

  private void waylandCheck() {
    String w = System.getenv("WAYLAND_DISPLAY");
    String x = System.getenv("XDG_SESSION_TYPE");

    if(w != null || "wayland".equalsIgnoreCase(x)) {
      glfwInitHint(GLFW_PLATFORM, GLFW_PLATFORM_X11);
    }
  }
}
