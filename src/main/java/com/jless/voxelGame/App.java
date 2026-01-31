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
  private Matrix4f projMat = new Matrix4f();
  private Matrix4f viewMat = new Matrix4f();

  private void init() {
    Window.create(Consts.W_WIDTH, Consts.W_HEIGHT, Consts.W_TITLE);
    Shaders.create();
    Rendering.create();

    ui = new UI();
    world = new World();
    threadManager = new ChunkThreadManager(world);
    world.generateSpawnAsync(threadManager);

    ui.initGUI(Window.getWindow());

    int maxWait = 50;
    int waited = 0;

    while(waited < maxWait) {
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      waited++;

      if(world.getChunkIfLoaded(0, 0) != null) {
        break;
      }
    }

    int spawnY = world.getSurfY(0, 0);
    if(spawnY > 0) {
      playerController = new PlayerController(0, spawnY + 2.0f, 0);
    } else {
      playerController = new PlayerController(0, Consts.WORLD_HEIGHT * 2.0f, 0);
    }

    player = new Player(world);

    setupMatrices();
    Input.setup(Window.getWindow());
  }

  private void setupMatrices() {
    float aspect = (float)Consts.W_WIDTH / Consts.W_HEIGHT;
    projMat = new Matrix4f().perspective((float)Math.toRadians(Consts.FOV), aspect, 0.1f, 500.0f);

    projMatrix = BufferUtils.createFloatBuffer(16);
    viewMatrix = BufferUtils.createFloatBuffer(16);

    projMat.get(projMatrix);
  }

  public void run() {
    waylandCheck();
    init();

    loop();

    cleanup();
  }

  private void loop() {
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
      playerController.getViewMatrix().get(viewMat);
      viewMat.get(viewMatrix);

      Input.update(player);
      player.blockManip();

      Rendering.beginFrame();

      Vector3f playerPos = PlayerController.pos;
      Rendering.renderWorld(world, playerPos, projMat, viewMat);
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
