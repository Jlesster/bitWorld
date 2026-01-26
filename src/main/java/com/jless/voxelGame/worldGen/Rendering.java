package com.jless.voxelGame.worldGen;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL20.*;

import java.nio.*;
import java.util.*;

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

  private static final FrustumIntersection frustum = new FrustumIntersection();
  private static final Matrix4f viewProjMat = new Matrix4f();

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
    glEnable(GL_DEPTH_TEST);
    Shaders.use();
    Shaders.setModelMatrix(Rendering.matrixBuffer);
    texBound = false;
  }

  public static void endFrame() {

  }

  public static void cleanup() {
    if(instance != null) {
      if(texAtlas != null) {
        texAtlas.cleanup();
      }
      instance = null;
    }
  }

  private static int debugFrameCount = 0;

  public static void renderWorld(World w, Vector3f playerPos) {
    ensureTexBound();
    rebuildDirty(w);

    int currProgram = glGetInteger(GL_CURRENT_PROGRAM);
    if(debugFrameCount % 60 == 0) {
      System.out.println("Curr shader program: " + currProgram);
    }

    int chunksRendered = 0;
    int totalChunks = 0;
    int uploadedChunks = 0;
    int visibleChunks = 0;
    int hasVertCount = 0;
    int hasVBOCount = 0;

    List<Chunk> chunkSnap = new ArrayList<>();
    synchronized(w.chunks) {
      chunkSnap.addAll(w.chunks.values());
    }

    debugFrameCount++;
    boolean printDebug = (debugFrameCount % 60 == 0);


    for(Chunk chunk : chunkSnap) {
      totalChunks++;
      if(chunk.uploaded) uploadedChunks++;
      if(chunk.vertCount > 0) hasVertCount++;
      if(chunk.vboID != 0) hasVBOCount++;

      boolean isVisible = Chunk.isChunkVisible(chunk, playerPos);
      if(isVisible) visibleChunks++;

      // Debug first chunk in detail (only occasionally)
      if(totalChunks == 1 && printDebug) {
        System.out.println("\n=== FIRST CHUNK DEBUG (Frame " + debugFrameCount + ") ===");
        System.out.println("  Position: (" + chunk.pos.x + ", " + chunk.pos.z + ")");
        System.out.println("  uploaded flag: " + chunk.uploaded);
        System.out.println("  vertCount: " + chunk.vertCount);
        System.out.println("  vboID: " + chunk.vboID);
        System.out.println("  vaoID: " + chunk.vaoID);
        System.out.println("  isVisible: " + isVisible);
      }

      if(chunk.uploaded && Chunk.isChunkVisible(chunk, playerPos)) {
        chunk.drawVBO();
        chunksRendered++;
      }
    }
    // Debug output (only occasionally to avoid spam)
    if(printDebug && totalChunks > 0) {
      System.out.println("\n=== RENDER STATS (Frame " + debugFrameCount + ") ===");
      System.out.println("Total: " + totalChunks);
      System.out.println("Uploaded: " + uploadedChunks);
      System.out.println("HasVertices: " + hasVertCount);
      System.out.println("HasVBO: " + hasVBOCount);
      System.out.println("Visible: " + visibleChunks);
      System.out.println("Rendered: " + chunksRendered);
      System.out.println("Player pos: " + playerPos);
    }
  }

  private static void rebuildDirty(World w) {
    List<Chunk> dirtyChunks = new ArrayList<>();
    synchronized(w.chunks) {
      for(Chunk chunk : w.chunks.values()) {
        if(chunk.dirty && chunk.uploaded) {
          dirtyChunks.add(chunk);
        }
      }
    }
    if(!dirtyChunks.isEmpty()) {
      System.out.println("Rebuilding");
    }

    for(Chunk chunk : dirtyChunks) {
      try {
        FloatBuffer data = chunk.buildMesh(w);
        if(data != null && data.limit() > 0) {
          chunk.uploadToGPU(data);
        } else {
          System.err.println("Empty mesh for dirty chunk");
        }
      } catch (Exception e) {
        System.err.println("Failed to rebuild dirty chunk");
        e.printStackTrace();
      }
    }
  }
}
