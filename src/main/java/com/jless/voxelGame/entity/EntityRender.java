package com.jless.voxelGame.entity;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.lang.Math;
import java.nio.*;

import org.lwjgl.*;

import org.joml.*;

import com.jless.voxelGame.blocks.*;
import com.jless.voxelGame.texture.*;

public class EntityRender {

  private static EntityRender instance;

  private int entityVAO = 0;
  private int entityVBO = 0;
  private int entityEBO = 0;

  private final Matrix4f modelMatrix = new Matrix4f();
  private final Vector3f colorVector = new Vector3f();
  private final FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);

  private static final float[] UNIT_CUBE_VERTICES = {
    // Front face (Z+) - format: x, y, z, nx, ny, nz, u, v, layer
    -0.5f, -0.5f,  0.5f,  0.0f,  0.0f,  1.0f,  0.0f, 1.0f, 0.0f,
     0.5f, -0.5f,  0.5f,  0.0f,  0.0f,  1.0f,  1.0f, 1.0f, 0.0f,
     0.5f,  0.5f,  0.5f,  0.0f,  0.0f,  1.0f,  1.0f, 0.0f, 0.0f,
    -0.5f,  0.5f,  0.5f,  0.0f,  0.0f,  1.0f,  0.0f, 0.0f, 0.0f,

    // Back face (Z-)
     0.5f, -0.5f, -0.5f,  0.0f,  0.0f, -1.0f,  0.0f, 1.0f, 0.0f,
    -0.5f, -0.5f, -0.5f,  0.0f,  0.0f, -1.0f,  1.0f, 1.0f, 0.0f,
    -0.5f,  0.5f, -0.5f,  0.0f,  0.0f, -1.0f,  1.0f, 0.0f, 0.0f,
     0.5f,  0.5f, -0.5f,  0.0f,  0.0f, -1.0f,  0.0f, 0.0f, 0.0f,

    // Top face (Y+)
    -0.5f,  0.5f,  0.5f,  0.0f,  1.0f,  0.0f,  0.0f, 1.0f, 0.0f,
     0.5f,  0.5f,  0.5f,  0.0f,  1.0f,  0.0f,  1.0f, 1.0f, 0.0f,
     0.5f,  0.5f, -0.5f,  0.0f,  1.0f,  0.0f,  1.0f, 0.0f, 0.0f,
    -0.5f,  0.5f, -0.5f,  0.0f,  1.0f,  0.0f,  0.0f, 0.0f, 0.0f,

    // Bottom face (Y-)
    -0.5f, -0.5f, -0.5f,  0.0f, -1.0f,  0.0f,  0.0f, 1.0f, 0.0f,
     0.5f, -0.5f, -0.5f,  0.0f, -1.0f,  0.0f,  1.0f, 1.0f, 0.0f,
     0.5f, -0.5f,  0.5f,  0.0f, -1.0f,  0.0f,  1.0f, 0.0f, 0.0f,
    -0.5f, -0.5f,  0.5f,  0.0f, -1.0f,  0.0f,  0.0f, 0.0f, 0.0f,

    // Right face (X+)
     0.5f, -0.5f,  0.5f,  1.0f,  0.0f,  0.0f,  0.0f, 1.0f, 0.0f,
     0.5f, -0.5f, -0.5f,  1.0f,  0.0f,  0.0f,  1.0f, 1.0f, 0.0f,
     0.5f,  0.5f, -0.5f,  1.0f,  0.0f,  0.0f,  1.0f, 0.0f, 0.0f,
     0.5f,  0.5f,  0.5f,  1.0f,  0.0f,  0.0f,  0.0f, 0.0f, 0.0f,

    // Left face (X-)
    -0.5f, -0.5f, -0.5f, -1.0f,  0.0f,  0.0f,  0.0f, 1.0f, 0.0f,
    -0.5f, -0.5f,  0.5f, -1.0f,  0.0f,  0.0f,  1.0f, 1.0f, 0.0f,
    -0.5f,  0.5f,  0.5f, -1.0f,  0.0f,  0.0f,  1.0f, 0.0f, 0.0f,
    -0.5f,  0.5f, -0.5f, -1.0f,  0.0f,  0.0f,  0.0f, 0.0f, 0.0f
  };

  private static final int[] CUBE_INDECES = {
    0, 1, 2, 0, 2, 3,
    4, 5, 6, 4, 6, 7,
    8, 9, 10, 8, 10, 11,
    12, 13, 14, 12, 14, 15,
    16, 17, 18, 16, 18, 19,
    20, 21, 22, 20, 22, 23
  };

  private EntityRender() {
    initGeometry();
  }

  public static void create() {
    if(instance != null) {
      throw new IllegalStateException("EntityRender already exists");
    }
    instance = new EntityRender();
  }

  public static EntityRender getInstance() {
    if(instance == null) {
      throw new IllegalStateException("Err: EntityRender does not exist");
    }
    return instance;
  }

  private void initGeometry() {
    FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(UNIT_CUBE_VERTICES.length);
    vertexBuffer.put(UNIT_CUBE_VERTICES);
    vertexBuffer.flip();

    IntBuffer indexBuffer = BufferUtils.createIntBuffer(CUBE_INDECES.length);
    indexBuffer.put(CUBE_INDECES);
    indexBuffer.flip();

    entityVAO = glGenVertexArrays();
    glBindVertexArray(entityVAO);

    entityVBO = glGenBuffers();
    glBindBuffer(GL_ARRAY_BUFFER, entityVBO);
    glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);

    entityEBO = glGenBuffers();
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, entityEBO);
    glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW);

    int stride = 9 * Float.BYTES;

    glEnableVertexAttribArray(0);
    glEnableVertexAttribArray(1);
    glEnableVertexAttribArray(2);
    glEnableVertexAttribArray(3);
    glVertexAttribPointer(0, 3, GL_FLOAT, false, stride, 0L);
    glVertexAttribPointer(1, 3, GL_FLOAT, false, stride, 3L * Float.BYTES);
    glVertexAttribPointer(2, 2, GL_FLOAT, false, stride, 6L * Float.BYTES);
    glVertexAttribPointer(3, 1, GL_FLOAT, false, stride, 8L * Float.BYTES);

    glBindBuffer(GL_ARRAY_BUFFER, 0);
    glBindVertexArray(0);
  }

  public static void drawEntityBox(
    float x0, float y0, float z0,
    float x1, float y1, float z1,
    float[] rgb
  ) {
    EntityRender renderer = getInstance();

    float minX = Math.min(x0, x1);
    float maxX = Math.max(x0, x1);
    float minY = Math.min(y0, y1);
    float maxY = Math.max(y0, y1);
    float minZ = Math.min(z0, z1);
    float maxZ = Math.max(z0, z1);

    float cx = (minX + maxX) * 0.5f;
    float cy = (minY + maxY) * 0.5f;
    float cz = (minZ + maxZ) * 0.5f;
    float sx = maxX - minX;
    float sy = maxY - minY;
    float sz = maxZ - minZ;

    renderer.modelMatrix.identity().translate(cx, cy, cz).scale(sx, sy, sz).get(renderer.matrixBuffer);

    try {
      Shaders.setModelMatrix(renderer.matrixBuffer);
      Shaders.setUniformInt("useSolidColor", 1);
      renderer.colorVector.set(rgb[0], rgb[1], rgb[2]);
      Shaders.setUniformVec("solidColor", renderer.colorVector);

      glBindVertexArray(renderer.entityVAO);
      glDrawElements(GL_TRIANGLES, 36, GL_UNSIGNED_INT, 0);
    } finally {
      glBindVertexArray(0);
      Shaders.setUniformInt("useSolidColor", 0);
    }
  }

  public static void drawEntityWithTransform(
    Matrix4f entityModel,
    float x0, float y0, float z0,
    float x1, float y1, float z1,
    float[] rgb
  ) {
    EntityRender renderer = getInstance();

    float minX = Math.min(x0, x1);
    float maxX = Math.max(x0, x1);
    float minY = Math.min(y0, y1);
    float maxY = Math.max(y0, y1);
    float minZ = Math.min(z0, z1);
    float maxZ = Math.max(z0, z1);

    float sx = maxX - minX;
    float sy = maxY - minY;
    float sz = maxZ - minZ;

    float cx = (minX + maxX) * 0.5f;
    float cy = (minY + maxY) * 0.5f;
    float cz = (minZ + maxZ) * 0.5f;

    renderer.modelMatrix.set(entityModel)
      .translate(cx, cy, cz)
      .scale(sx, sy, sz)
      .get(renderer.matrixBuffer);

    try {
      Shaders.setModelMatrix(renderer.matrixBuffer);
      Shaders.setUniformInt("useSolidColor", 1);
      renderer.colorVector.set(rgb[0], rgb[1], rgb[2]);
      Shaders.setUniformVec("solidColor", renderer.colorVector);

      glBindVertexArray(renderer.entityVAO);
      glDrawElements(GL_TRIANGLES, 36, GL_UNSIGNED_INT, 0);
    } finally {
      glBindVertexArray(0);
      Shaders.setUniformInt("useSolidColor", 0);
    }
  }

  public static void drawTexturedEntityWithTransform(
    Matrix4f entityModel,
    float x0, float y0, float z0,
    float x1, float y1, float z1,
    int texTile
  ) {
    EntityRender renderer = getInstance();

    float minX = Math.min(x0, x1);
    float maxX = Math.max(x0, x1);
    float minY = Math.min(y0, y1);
    float maxY = Math.max(y0, y1);
    float minZ = Math.min(z0, z1);
    float maxZ = Math.max(z0, z1);

    float cx = (minX + maxX) * 0.5f;
    float cy = (minY + maxY) * 0.5f;
    float cz = (minZ + maxZ) * 0.5f;
    float sx = maxX - minX;
    float sy = maxY - minY;
    float sz = maxZ - minZ;

    renderer.modelMatrix.set(entityModel)
      .translate(cx, cy, cz)
      .scale(sx, sy, sz)
      .get(renderer.matrixBuffer);

    Shaders.setModelMatrix(renderer.matrixBuffer);
    Shaders.setUniformInt("useSolidColor", 0);
    // Shaders.setUniformFloat("uLayer", TextureAtlas.toLayer(texTile));

    glBindVertexArray(renderer.entityVAO);
    glDrawElements(GL_TRIANGLES, 36, GL_UNSIGNED_INT, 0);
    glBindVertexArray(0);
  }

  public static void drawTexturedEntityBox(
    float x0, float y0, float z0,
    float x1, float y1, float z1,
    int texTile
  ) {
    EntityRender renderer = getInstance();

    float minX = Math.min(x0, x1);
    float maxX = Math.max(x0, x1);
    float minY = Math.min(y0, y1);
    float maxY = Math.max(y0, y1);
    float minZ = Math.min(z0, z1);
    float maxZ = Math.max(z0, z1);

    float cx = (minX + maxX) * 0.5f;
    float cy = (minY + maxY) * 0.5f;
    float cz = (minZ + maxZ) * 0.5f;
    float sx = maxX - minX;
    float sy = maxY - minY;
    float sz = maxZ - minZ;

    renderer.modelMatrix.identity().translate(cx, cy, cz).scale(sx, sy, sz).get(renderer.matrixBuffer);

    Shaders.setModelMatrix(renderer.matrixBuffer);
    Shaders.setUniformInt("useSolidColor", 0);
    Shaders.setUniformFloat("uLayer", TextureAtlas.toLayer(texTile));

    glBindVertexArray(renderer.entityVAO);
    glDrawElements(GL_TRIANGLES, 36, GL_UNSIGNED_INT, 0);
    glBindVertexArray(0);
  }

  public static void drawBlockItem(Matrix4f entityModel, byte blockID) {
    EntityRender renderer = getInstance();

    int id = blockID & 0xFF;

    int top = TextureAtlas.toLayer(Blocks.TEX_TOP[id]);
    int bottom = TextureAtlas.toLayer(Blocks.TEX_BOTTOM[id]);
    int side = TextureAtlas.toLayer(Blocks.TEX_SIDE[id]);
    int front = TextureAtlas.toLayer(Blocks.TEX_FRONT[id]);

    int[] layers = new int[] {
      front,
      side,
      top,
      bottom,
      side,
      side
    };

    renderer.modelMatrix.set(entityModel).get(renderer.matrixBuffer);
    Shaders.setModelMatrix(renderer.matrixBuffer);
    Shaders.setUniformInt("useSolidColor", 0);

    glBindVertexArray(renderer.entityVAO);

    for(int face = 0; face < 6; face++) {
      Shaders.setTexLayer(layers[face]);
      long offset = face * 6L * Integer.BYTES;
      glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, offset);
    }
    glBindVertexArray(0);
  }

  public static void resetModelMatrix() {
    EntityRender renderer = getInstance();
    renderer.modelMatrix.identity().get(renderer.matrixBuffer);
    Shaders.setModelMatrix(renderer.matrixBuffer);
  }

  public static void cleanup() {
    if(instance != null) {
      glDeleteVertexArrays(instance.entityVAO);
      glDeleteBuffers(instance.entityVBO);
      glDeleteBuffers(instance.entityEBO);
      instance = null;
    }
  }
}
