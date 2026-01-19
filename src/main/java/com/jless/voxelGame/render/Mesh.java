package com.jless.voxelGame.render;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.*;

public class Mesh {

  private final int vao;
  private final int vbo;
  private final int ebo;
  private final int indexCount;

  public Mesh(float[] vertices, int[] indices) {
    indexCount = indices.length;

    vao = glGenVertexArrays();
    glBindVertexArray(vao);

    vbo = glGenBuffers();
    glBindBuffer(GL_ARRAY_BUFFER, vbo);
    glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

    ebo = glGenBuffers();
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
    glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);

    int stride = 5 * Float.BYTES;

    glVertexAttribPointer(0, 3, GL_FLOAT, false, stride, 0l);
    glEnableVertexAttribArray(0);

    glVertexAttribPointer(1, 2, GL_FLOAT, false, stride, 3L * Float.BYTES);
    glEnableVertexAttribArray(1);

    glBindVertexArray(0);
  }

  public void render() {
    glBindVertexArray(vao);
    glDrawElements(GL_TRIANGLES, indexCount, GL_UNSIGNED_INT, 0L);
    glBindVertexArray(0);
  }

  public void destroy() {
    glDeleteBuffers(vbo);
    glDeleteBuffers(ebo);
    glDeleteVertexArrays(vao);
  }
}
