package com.jless.voxelGame;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL30.*;

import java.lang.Math;
import java.nio.*;

import org.lwjgl.*;

import org.joml.*;

import com.jless.voxelGame.chunkGen.*;
import com.jless.voxelGame.entity.*;
import com.jless.voxelGame.lighting.*;
import com.jless.voxelGame.player.*;
import com.jless.voxelGame.texture.*;
import com.jless.voxelGame.ui.*;
import com.jless.voxelGame.worldGen.*;

public class App {

  private UI ui;
  private World world;
  private Player player;
  private Lighting lighting;
  private ChunkThreadManager threadManager;
  private PlayerController playerController;
  private FloatBuffer projMatrix;
  private FloatBuffer viewMatrix;
  private FloatBuffer lightProjMatrix;
  private FloatBuffer lightViewMatrix;
  private Matrix4f projMat = new Matrix4f();
  private Matrix4f viewMat = new Matrix4f();
  private EntityManager entityManager = new EntityManager();

  private double lastTime = 0.0;
  private double currTime = 0.0;
  private float smoothedDT = 0.016f;
  private float worldWarmupTimer = 0f;

  private void init() {
    Window.create(Consts.W_WIDTH, Consts.W_HEIGHT, Consts.W_TITLE);
    Shaders.create();

    lighting = new Lighting();
    lighting.initShadowMapping();

    Shaders.cacheLightingUniforms();
    Rendering.create();
    EntityRender.create();

    ui = new UI();
    world = new World();
    threadManager = new ChunkThreadManager(world);
    world.generateSpawnAsync(threadManager);
    ui.initGUI(Window.getWindow());

    entityManager.init(world);

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
      playerController = new PlayerController(0, Consts.WORLD_HEIGHT, 0);
    }

    player = new Player(world);

    setupMatrices();
    Input.setup(Window.getWindow());
    lastTime = glfwGetTime();
  }

  private void setupMatrices() {
    float aspect = (float)Consts.W_WIDTH / Consts.W_HEIGHT;
    projMat = new Matrix4f().perspective((float)Math.toRadians(Consts.FOV), aspect, 0.1f, 500.0f);

    projMatrix = BufferUtils.createFloatBuffer(16);
    viewMatrix = BufferUtils.createFloatBuffer(16);
    lightProjMatrix = BufferUtils.createFloatBuffer(16);
    lightViewMatrix = BufferUtils.createFloatBuffer(16);

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
      currTime = glfwGetTime();
      float rawDT = (float)(currTime - lastTime);
      lastTime = currTime;
      rawDT = Math.min(rawDT, 0.1f);

      smoothedDT = smoothedDT * (1.0f - Consts.DT_SMOOTHING_FACTOR) + rawDT * Consts.DT_SMOOTHING_FACTOR;
      float dt = smoothedDT;

      worldWarmupTimer += dt;
      boolean allowStreaming = worldWarmupTimer > 3.0f;

      Window.update();
      threadManager.processUploads();
      world.processPostProcessQueue();

      lighting.update(dt);
      Shaders.use();

      Shaders.setSunDir(lighting.getSunDir());
      Shaders.setLightColor(lighting.getLightColor());
      Shaders.setAmbientColor(lighting.getAmbientColor());
      Shaders.setLightSpaceMatrix(lighting.getLightSpaceMatrix());
      Shaders.setShadowEnabled(lighting.areShadowsEnabled());

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

      Vector3f sky = lighting.getSkyColor();
      glClearColor(sky.x, sky.y, sky.z, 1.0f);

      if(lighting.areShadowsEnabled()) renderShadowPass();

      if(lighting.areShadowsEnabled()) {
        lighting.bindShadowMap(1);
        Shaders.setShadowMap(1);
      }

      boolean jumpPressed = Input.isKeyPressed(GLFW_KEY_SPACE);
      playerController.update(world, dt, jumpPressed, threadManager);
      playerController.getViewMatrix().get(viewMat);
      viewMat.get(viewMatrix);

      Input.update(player);
      player.blockManip();
      player.updateHand(dt, playerController.velocity);

      Rendering.beginFrame();

      Vector3f playerPos = PlayerController.pos;
      Rendering.renderWorld(world, playerPos, projMat, viewMat);

      ui.renderGUI();
      entityManager.update(world, playerPos, dt);
      entityManager.render(Rendering.getRenderer());

      glActiveTexture(GL_TEXTURE0);
      glBindTexture(GL_TEXTURE_2D_ARRAY, Rendering.textureArrayID);
      Shaders.setTextureUnit(0);
      player.renderHand();

      Shaders.setViewMatrix(viewMatrix);
      glfwSwapBuffers(Window.getWindow());
    }
  }

  private void renderShadowPass() {
    Matrix4f lightProj = lighting.getLightProjMatrix();
    Matrix4f lightView = lighting.getLightViewMatrix();

    lightProj.get(lightProjMatrix);
    lightView.get(lightViewMatrix);

    lighting.beginShadowPass(PlayerController.pos);

    Shaders.setProjMatrix(lightProjMatrix);
    Shaders.setViewMatrix(lightViewMatrix);

    glColorMask(false, false, false, false);

    for(Chunk chunk : world.chunks.values()) {
      if(chunk.uploaded && Chunk.isChunkVisible(chunk, PlayerController.pos)) {
        chunk.drawVBO();
      }
    }

    entityManager.render(Rendering.getRenderer());

    glColorMask(true, true, true, true);
    glDepthMask(true);

    lighting.endShadowPass(Consts.W_WIDTH, Consts.W_HEIGHT);

    Shaders.setProjMatrix(projMatrix);
    Shaders.setViewMatrix(viewMatrix);
    EntityRender.resetModelMatrix();
  }

  private void cleanup() {
    Window.destroy();
    lighting.cleanup();
    Rendering.cleanup();
    Shaders.cleanup();
    EntityRender.cleanup();
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
