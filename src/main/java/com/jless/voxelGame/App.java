package com.jless.voxelGame;

import static org.lwjgl.opengl.GL11.*;

import com.jless.voxelGame.player.Player;
import com.jless.voxelGame.player.PlayerController;
import com.jless.voxelGame.render.Camera;

public class App {

  private Window window;

  private Player player;
  private PlayerController controller;
  private Camera camera;

  public void run() {
    init();
    loop();
    cleanup();
  }

  private void init() {
    window = new Window(Consts.WINDOW_WIDTH, Consts.WINDOW_HEIGHT, Consts.WINDOW_TITLE);

    Time.init();
    Input.init(window.window());

    Input.captureMouse(true);

    player = new Player();
    controller = new PlayerController(player);

    camera = new Camera();
    camera.setGluPersp(Consts.FOV, (float)window.width() / (float)window.height(), 0.05f, 1000.0f);
  }

  private void loop() {
    while(!window.shouldClose()) {
      Time.update();
      float dt = Time.dt();

      update(dt);

      glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
      render();

      window.update();
      Input.endFrame();
    }
  }

  public void render() {
    //TODO draw calls
  }

  private void update(float dt) {
    controller.update(dt);
    camera.updateView(player.position, player.yaw, player.pitch);
  }

  private void cleanup() {
    window.destroy();
  }

  public static void main(String[] args) {
    new App().run();
  }
}
