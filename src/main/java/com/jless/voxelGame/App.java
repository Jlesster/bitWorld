package com.jless.voxelGame;

import static org.lwjgl.opengl.GL11.*;

public class App {

  private Window window;

  public void run() {
    init();
    loop();
    cleanup();
  }

  private void init() {
    window = new Window(Consts.WINDOW_WIDTH, Consts.WINDOW_HEIGHT, Consts.WINDOW_TITLE);
    Time.init();
  }

  private void loop() {
    while(!window.shouldClose()) {
      Time.update();
      float dt = Time.dt();

      update(dt);

      glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
      render();

      window.update();
    }
  }

  public void render() {
    //TODO draw calls
  }

  private void update(float dt) {
    //TODO input, player, world updates
  }

  private void cleanup() {
    window.destroy();
  }

  public static void main(String[] args) {
    new App().run();
  }
}
