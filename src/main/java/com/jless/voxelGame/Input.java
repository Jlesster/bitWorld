package com.jless.voxelGame;

import static org.lwjgl.glfw.GLFW.*;

public final class Input {

  private static long window;

  private static double mouseX, mouseY;
  private static double lastMouseX, lastMouseY;
  private static float mouseDX, mouseDY;

  private static boolean firstMouse = true;
  private static boolean captured = false;

  private static final boolean[] keysDown = new boolean[512];
  private static final boolean[] keysPressed = new boolean[512];

  public static void init(long window) {
    Input.window = window;

    glfwSetKeyCallback(window, (w, key, scancode, action, mods) -> {
      if(key < 0 || key >= keysDown.length) return;

      if(action == GLFW_PRESS) {
        keysDown[key] = true;
        keysPressed[key] = true;
      } else if(action == GLFW_RELEASE) {
        keysDown[key] = false;
      }
    });

    glfwSetCursorPosCallback(window, (w, xpos, ypos) -> {
      System.out.println("mouse moved: " + xpos + ", " + ypos);
      mouseX = xpos;
      mouseY = ypos;

      if(firstMouse) {
        lastMouseX = mouseX;
        lastMouseY = mouseY;
        firstMouse = false;
      }

      mouseDX = (float)(mouseX - lastMouseX);
      mouseDY = (float)(mouseY - lastMouseY);

      lastMouseX = mouseX;
      lastMouseY = mouseY;
    });

    glfwSetMouseButtonCallback(window, (w, button, action, mods) -> {
      if(button == GLFW_MOUSE_BUTTON_1 && action == GLFW_PRESS) {
        captureMouse(true);
      }
    });
  }

  public static void update() {
    for(int i = 0; i < keysPressed.length; i++) {
      keysPressed[i] = false;
    }
  }

  public static void endFrame() {
    mouseDX = 0;
    mouseDY = 0;
  }

  public static boolean down(int key) {
    return keysDown[key];
  }

  public static boolean pressed(int key) {
    return keysPressed[key];
  }

  public static float mouseDX() { return mouseDX; }
  public static float mouseDY() { return mouseDY; }

  public static boolean isCaptured() {
    return captured;
  }

  public static void captureMouse(boolean shouldCap) {
    captured = shouldCap;

    glfwSetInputMode(window, GLFW_CURSOR, shouldCap ? GLFW_CURSOR_DISABLED : GLFW_CURSOR_NORMAL);

    firstMouse = true;
  }

  private Input() {}
}
