package com.jless.voxelGame;

import static org.lwjgl.opengl.GL11.*;

import org.joml.Matrix4f;

import com.jless.voxelGame.player.Player;
import com.jless.voxelGame.player.PlayerController;
import com.jless.voxelGame.render.*;
import com.jless.voxelGame.world.BlockID;
import com.jless.voxelGame.world.World;

public class App {

  private Window window;

  private Player player;
  private PlayerController controller;
  private Camera camera;
  private Mesh qMesh;
  private Texture texture;
  private ShaderProgram shader;
  private TextureAtlas atlas;

  private int atlasX = 0;
  private int atlasY = 9;

  private final Matrix4f model = new Matrix4f();

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

    shader = new ShaderProgram("shaders/simple.vert", "shaders/simple.frag");

    texture = new Texture("Tileset.png");
    atlas = new TextureAtlas(texture, 16);
    rebuildQuadForTile(atlasX, atlasY);

    World world = new World();
    for(int x = -5; x <= 5; x++) {
      for(int z = -5; z <= 5; z++) {
        world.setBlock(x, 79, z, BlockID.STONE);
      }
    }
    world.setBlock(0, 79, 0, BlockID.STONE);

    System.out.println("Block at 0,79,0 = " + world.getBlock(0, 79, 0));
    System.out.println("Block at 0,80,0 = " + world.getBlock(0, 80, 0));

    model.identity().translate(0, 0, -2).scale(2.0f);
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
    shader.bind();

    shader.setMat4("uProj", camera.projection());
    shader.setMat4("uView", camera.view());
    shader.setMat4("uModel", model);

    texture.bind(0);
    shader.setInt("uTex", 0);

    qMesh.render();
    shader.unbind();
  }

  private void rebuildQuadForTile(int tileX, int tileY) {
    float[] uv = atlas.getUVRect(tileX, tileY);

    float u0 = uv[0];
    float v0 = uv[1];
    float u1 = uv[2];
    float v1 = uv[3];

    float[] verts = {
      -0.5f, -0.5f, 0.0f,   u0, v0,
       0.5f, -0.5f, 0.0f,   u1, v0,
       0.5f,  0.5f, 0.0f,   u1, v1,
      -0.5f,  0.5f, 0.0f,   u0, v1,
    };

    int[] inds = {
      0,1,2,
      2,3,0
    };

    if(qMesh != null) {
      qMesh.destroy();
    }

    qMesh = new Mesh(verts, inds);

  }

  private void update(float dt) {
    controller.update(dt);
    camera.updateView(player.position, player.yaw, player.pitch);

    if(Input.pressed(org.lwjgl.glfw.GLFW.GLFW_KEY_W)) {
      atlasX++;
      if(atlasX >= atlas.tilesX()) atlasX = 0;
      rebuildQuadForTile(atlasX, atlasY);
    }
  }

  private void cleanup() {
    qMesh.destroy();
    texture.destroy();
    shader.destory();
    window.destroy();
  }

  public static void main(String[] args) {
    new App().run();
  }
}
