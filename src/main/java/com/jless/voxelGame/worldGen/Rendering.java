package com.jless.voxelGame.worldGen;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;

import java.nio.*;

import org.lwjgl.*;

import org.joml.*;

import com.jless.voxelGame.chunkGen.*;
import com.jless.voxelGame.texture.*;

public class Rendering {

  private static Rendering instance;
  private static TextureLoader texAtlas;
  private static boolean texBound = false;

  private static final Matrix4f identityMatrix = new Matrix4f();
  private static final FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);

  private Rendering() {
    validateShadersCreated();
    initOpenGL();
    loadTexAtlas();
    setupIdentityMatrix();
  }

  private void setupIdentityMatrix() {
    identityMatrix.identity().get(matrixBuffer);
  }

  private void loadTexAtlas() {
    texAtlas = TextureLoader.loadResource("/Tileset.png", false);
  }

  private static void ensureTexBound() {
    if(!texBound) {
      glActiveTexture(GL_TEXTURE0);
      texAtlas.bind();
      Shaders.setTextureUnit(0);
      texBound = true;
    }
  }

  private void initOpenGL() {
    glEnable(GL_DEPTH_TEST);
    glEnable(GL_CULL_FACE);
    glCullFace(GL_BACK);
    glFrontFace(GL_CCW);
    glClearColor(0.5f, 0.7f, 1.0f, 1.0f);
  }

  private void validateShadersCreated() {
    if(Shaders.instance == null) {
      throw new RuntimeException("Shaders not Initialised");
    }
  }

  public static void create() {
    if(instance != null) {
      throw new IllegalStateException("Err: Textures already created");
    }
    instance = new Rendering();
  }

  public static void beginFrame() {
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    Shaders.use();
    Shaders.setModelMatrix(Rendering.matrixBuffer);
    texBound = false;
  }

  public static void endFrame() {}

  public static void cleanup() {
    if(instance != null) {
      if(texAtlas != null) {
        texAtlas.cleanup();
      }
      instance = null;
    }
  }

  public static void renderWorld(World w, Vector3f playerPos) {
    ensureTexBound();

    for(Chunk chunk : w.getLoadedChunks()) {
      if(Chunk.isChunkVisible(chunk, playerPos)) {
        chunk.ensureUploaded(w);
        chunk.drawVBO();
      }
    }
  }
}
