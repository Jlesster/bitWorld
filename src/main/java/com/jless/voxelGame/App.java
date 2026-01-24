package com.jless.voxelGame;

import static org.lwjgl.glfw.GLFW.*;

import com.jless.voxelGame.texture.*;
import com.jless.voxelGame.worldGen.*;

public class App {

  private void init() {
    Window.create(Consts.W_WIDTH, Consts.W_HEIGHT, Consts.W_TITLE);
    Shaders.create();
    Shaders.use();
    Shaders.setViewMatrix(viewMatrix);
    Rendering.create();
  }

  public void run() {
    waylandCheck();
    init();

    loop();

    cleanup();
  }

  private void loop() {
    while(!glfwWindowShouldClose(Window.getWindow())) {
      Rendering.beginFrame();
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
