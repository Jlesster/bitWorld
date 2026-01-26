package com.jless.voxelGame;

import static org.lwjgl.glfw.GLFW.*;

import com.jless.voxelGame.player.*;

public class Input {

	private static boolean[] keys = new boolean[GLFW_KEY_LAST];
	private static boolean[] mouseButtons = new boolean[GLFW_MOUSE_BUTTON_LAST];
	private static double mouseX, mouseY;
	private static double lastMouseX, lastMouseY;
	private static boolean firstMouse = true;

	public static void update() {
		lastMouseX = mouseX;
		lastMouseY = mouseY;
	}

  public static void setup(long w) {
  	glfwSetKeyCallback(w, (window, key, scancode, action, mods) -> {
			if(key >= 0 && key < keys.length) {
				keys[key] = (action == GLFW_PRESS || action == GLFW_REPEAT);
			}
  	});

  	glfwSetMouseButtonCallback(w, (window, button, action, mods) -> {
  		if(button >= 0 && button < mouseButtons.length) {
  			mouseButtons[button] = (action == GLFW_PRESS);
  		}
  		if(button == GLFW_MOUSE_BUTTON_LEFT && action == GLFW_PRESS) {
  			Player.breakReq = true;
  		}
  	});

  	glfwSetCursorPosCallback(w, (window, xpos, ypos) -> {
  		if(firstMouse) {
  			lastMouseX = xpos;
  			lastMouseY = ypos;
  			firstMouse = false;
  		}
  		mouseX = xpos;
  		mouseY = ypos;
  	});

  	glfwSetInputMode(w, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
  }

  public static boolean isKeyPressed(int key) {
  	return key >= 0 && key < keys.length && keys[key];
  }

	private static boolean isMousePressed(int button) {
		return button >= 0 && button < mouseButtons.length && mouseButtons[button];
	}

	public static float getMouseDX() {
		return (float)(mouseX - lastMouseX);
	}

	public static float getMouseDY() {
		return (float)(mouseY - lastMouseY);
	}
}
