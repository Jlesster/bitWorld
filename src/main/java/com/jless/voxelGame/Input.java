package com.jless.voxelGame;

import static org.lwjgl.glfw.GLFW.*;

import com.jless.voxelGame.blocks.*;
import com.jless.voxelGame.player.*;

public class Input {

	private static boolean[] keys = new boolean[GLFW_KEY_LAST];
	private static boolean[] keyDown = new boolean[GLFW_KEY_LAST];

	private static boolean[] mouseButtons = new boolean[GLFW_MOUSE_BUTTON_LAST];
	private static double mouseX, mouseY;
	private static double lastMouseX, lastMouseY;
	private static boolean firstMouse = true;
	private static boolean lmbPressed = false;
	private static boolean rmbPressed = false;

	public static void update(Player player) {
		lastMouseX = mouseX;
		lastMouseY = mouseY;

		handleHotbarInput(player);
		handleMouseInput(player);

		for(int i = 0; i < keyDown.length; i++) {
			keyDown[i] = false;
		}
	}

  public static void setup(long w) {
  	glfwSetKeyCallback(w, (window, key, scancode, action, mods) -> {
			if(key >= 0 && key < keys.length) {
				if(action == GLFW_PRESS) {
					keys[key] = true;
					keyDown[key] = true;
				} else if(action == GLFW_RELEASE) {
					keys[key] = false;
				}
			}
  	});

  	glfwSetMouseButtonCallback(w, (window, button, action, mods) -> {
  		if(button >= 0 && button < mouseButtons.length) {
  			mouseButtons[button] = (action == GLFW_PRESS);
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

	private static void handleMouseInput(Player player) {
		if(isMousePressed(GLFW_MOUSE_BUTTON_RIGHT) && Player.placeReq == false) Player.placeReq = true;
		if(isMousePressed(GLFW_MOUSE_BUTTON_LEFT) && Player.breakReq == false) Player.breakReq = true;
	}

	public static void handleHotbarInput(Player player) {
		if(isKeyPressed(GLFW_KEY_1)) player.setSelectedBlock(BlockID.GRASS);
		if(isKeyPressed(GLFW_KEY_2)) player.setSelectedBlock(BlockID.DIRT);
		if(isKeyPressed(GLFW_KEY_3)) player.setSelectedBlock(BlockID.COBBLE);
		if(isKeyPressed(GLFW_KEY_4)) player.setSelectedBlock(BlockID.STONE);
		if(isKeyPressed(GLFW_KEY_5)) player.setSelectedBlock(BlockID.SAND);
		if(isKeyPressed(GLFW_KEY_6)) player.setSelectedBlock(BlockID.GLASS);
		if(isKeyPressed(GLFW_KEY_7)) player.setSelectedBlock(BlockID.OAK_PLANK);
		if(isKeyPressed(GLFW_KEY_8)) player.setSelectedBlock(BlockID.OAK_LOG);
		if(isKeyPressed(GLFW_KEY_9)) player.setSelectedBlock(BlockID.OAK_LEAVES);
	}

	public static boolean isMousePressed(int button) {
		boolean currState = glfwGetMouseButton(Window.getWindow(), button) == GLFW_PRESS;
		boolean wasPressed = false;

		if(button == GLFW_MOUSE_BUTTON_LEFT) {
			wasPressed = currState && !lmbPressed;
			lmbPressed = currState;
		} else if(button == GLFW_MOUSE_BUTTON_RIGHT) {
			wasPressed = currState && !rmbPressed;
			rmbPressed = currState;
		}
		return wasPressed;
	}

  public static boolean isKeyPressed(int key) {
  	return key >= 0 && key < keys.length && keys[key];
  }

	public static float getMouseDX() {
		return (float)(mouseX - lastMouseX);
	}

	public static float getMouseDY() {
		return (float)(mouseY - lastMouseY);
	}
}
