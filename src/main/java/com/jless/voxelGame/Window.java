package com.jless.voxelGame;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;

public class Window {

  private long window;
  private int width;
  private int height;

  public Window(int width, int height, String title) {
    this.width = width;
    this.height = height;

    waylandCheck();

    GLFWErrorCallback.createPrint(System.err).set();
    if(!glfwInit()) {
      throw new IllegalStateException("Failed to init GLFW");
    }

    glfwDefaultWindowHints();
    glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
    glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

    window = glfwCreateWindow(width, height, title, NULL, NULL);
    if(window == NULL) {
      throw new RuntimeException("Failed to create GLFW Window");
    }

    glfwMakeContextCurrent(window);
    glfwSwapInterval(Consts.VSYNC ? 1 : 0);

    glfwShowWindow(window);

    GL.createCapabilities();
    glEnable(GL_DEPTH_TEST);
    glEnable(GL_CULL_FACE);
    glCullFace(GL_BACK);

    glClearColor(0.1f, 0.1f, 0.12f, 1.0f);
  }

  public boolean shouldClose() {
    return glfwWindowShouldClose(window);
  }

  public void update() {
    glfwSwapBuffers(window);
    glfwPollEvents();
  }

  public void destroy() {
    glfwDestroyWindow(window);
    glfwTerminate();
  }

  public long window() {
    return window;
  }

  private void waylandCheck() {
    String w = System.getenv("WAYLAND_DISPLAY_TYPE");
    String x = System.getenv("XDG_SESSION_TYPE");

    if(w != null | "wayland".equalsIgnoreCase(x)) {
      glfwInitHint(GLFW_PLATFORM, GLFW_PLATFORM_X11);
    }
  }

  public int width() { return width; }
  public int height() { return height; }
}
