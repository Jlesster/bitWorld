package com.jless.voxelGame;

import static org.lwjgl.glfw.GLFW.*;

import java.lang.Math;
import java.nio.*;

import org.lwjgl.*;

import org.joml.*;

import com.jless.voxelGame.player.*;
import com.jless.voxelGame.texture.*;
import com.jless.voxelGame.ui.*;
import com.jless.voxelGame.worldGen.*;

public class App {

  private UI ui;
  private World world;
  private Player player;
  private ChunkThreadManager threadManager;
  private PlayerController playerController;
  private FloatBuffer projMatrix;
  private FloatBuffer viewMatrix;

  private void init() {
    Window.create(Consts.W_WIDTH, Consts.W_HEIGHT, Consts.W_TITLE);
    Shaders.create();
    Rendering.create();

    ui = new UI();
    world = new World();
    playerController = new PlayerController(0, Consts.WORLD_HEIGHT * 1.0f, 0);
    player = new Player(world);
    threadManager = new ChunkThreadManager(world);

    ui.initGUI(Window.getWindow());
    world.generateSpawnAsync(threadManager);
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
    System.out.println("Main render loop starting on thread: " + Thread.currentThread().getName());
    while(!glfwWindowShouldClose(Window.getWindow())) {
      Window.update();
      threadManager.processUploads();

      Shaders.use();

      Shaders.setFogParams(
        Consts.FOG_COLOR_R,
        Consts.FOG_COLOR_G,
        Consts.FOG_COLOR_B,
        Consts.FOG_START,
        Consts.FOG_END
      );

      Shaders.setViewMatrix(viewMatrix);
      Shaders.setProjMatrix(projMatrix);
      Shaders.setCameraPos(PlayerController.pos);

      boolean jumpPressed = Input.isKeyPressed(GLFW_KEY_SPACE);
      playerController.update(world, 0.016f, jumpPressed, threadManager);
      playerController.getViewMatrix().get(viewMatrix);

      Input.update();
      player.blockManip();

      Rendering.beginFrame();

      Vector3f playerPos = PlayerController.pos;
      Rendering.renderWorld(world, playerPos);
      ui.renderGUI();

      glfwSwapBuffers(Window.getWindow());
    }
  }

  private void cleanup() {
    Window.destroy();
    Rendering.cleanup();
    Shaders.cleanup();
    ChunkThreadManager.cleanup();
    ThreadSafePerlin.cleanup();
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
