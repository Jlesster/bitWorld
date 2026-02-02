package com.jless.voxelGame.worldGen;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL30.*;

import java.nio.*;
import java.util.*;

import org.lwjgl.*;

import org.joml.*;

import com.jless.voxelGame.*;
import com.jless.voxelGame.chunkGen.*;
import com.jless.voxelGame.texture.*;

public class Rendering {

  private static Rendering instance;
  private static boolean texBound = false;

  private static final Matrix4f identityMatrix = new Matrix4f();
  private static final FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);

  public static final FrustumIntersection frustum = new FrustumIntersection();
  private static final Matrix4f viewProjMat = new Matrix4f();
  public static int textureArrayID = TextureLoader.loadTexArray("/Tileset.png", 16);

  private Rendering() {
    validateShadersCreated();
    initOpenGL();
    setupIdentityMatrix();
  }

  private void setupIdentityMatrix() {
    identityMatrix.identity().get(matrixBuffer);
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

  public static void updateFrustum(Matrix4f projMat, Matrix4f viewMat) {
    viewProjMat.set(projMat).mul(viewMat);
    frustum.set(viewProjMat);
  }

  public static void beginFrame() {
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    glEnable(GL_DEPTH_TEST);
    Shaders.use();
    Shaders.setModelMatrix(Rendering.matrixBuffer);
    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D_ARRAY, textureArrayID);
    Shaders.setTextureUnit(0);
    texBound = true;
  }

  private static boolean isChunkInRenderDistance(Chunk chunk, Vector3f playerPos) {
    float wx = chunk.pos.x * Consts.CHUNK_SIZE;
    float wz = chunk.pos.z * Consts.CHUNK_SIZE;

    float dx = (wx + Consts.CHUNK_SIZE * 0.5f) - playerPos.x;
    float dz = (wz + Consts.CHUNK_SIZE * 0.5f) - playerPos.z;

    float distSq = dx * dx + dz * dz;
    float maxDist = Consts.RENDER_DISTANCE * Consts.CHUNK_SIZE;

    return distSq <= (maxDist * maxDist);
  }

  public static Rendering getRenderer() {
    return instance;
  }

  public static void cleanup() {
    if(instance != null) {
      instance = null;
    }
  }

  private static int debugFrameCount = 0;

  public static void renderWorld(World w, Vector3f playerPos, Matrix4f projMat, Matrix4f viewMat) {
    updateFrustum(projMat, viewMat);
    List<Chunk> chunkSnap = new ArrayList<>();
    synchronized(w.chunks) {
      chunkSnap.addAll(w.chunks.values());
    }

    for(Chunk chunk : chunkSnap) {

      boolean isVisible = Chunk.isChunkVisible(chunk, playerPos);
      if(chunk.uploaded && isVisible) {
        chunk.drawVBO();
      }
    }
  }
}
