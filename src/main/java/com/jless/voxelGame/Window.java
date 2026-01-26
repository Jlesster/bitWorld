package com.jless.voxelGame;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.*;

import org.lwjgl.opengl.*;

public class Window {
  private static Window instance;
  private final long window;

  private Window(int w, int h, String title) {
    if(!glfwInit()) {
      throw new IllegalStateException("Err: failed to init GLFW");
    }

    glfwDefaultWindowHints();
    glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
    glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

    window = glfwCreateWindow(w, h, title, NULL, NULL);

    if(window == NULL) {
      throw new RuntimeException("Err: Failed to create window");
    }

    glfwMakeContextCurrent(window);
    glfwSwapInterval(Consts.VSYNC);
    glfwShowWindow(window);
    GL.createCapabilities();
  }

  public static void create(int w, int h, String title) {
    if(instance != null) {
      throw new IllegalStateException("Window already created");
    }
    instance = new Window(w, h, title);
  }

  public static long getWindow() {
    if(instance == null) {
      throw new IllegalStateException("Window not created");
    }
    return instance.window;
  }

  public static void destroy() {
    if(instance != null) {
      glfwDestroyWindow(instance.window);
      glfwTerminate();
      instance = null;
    }
  }

  public static boolean shouldClose() {
    return glfwWindowShouldClose(instance.window);
  }

  public static void update() {
    glfwPollEvents();
  }

}
