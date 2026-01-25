package com.jless.voxelGame.texture;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

import java.io.*;
import java.nio.*;

import org.lwjgl.system.*;

public class Shaders {

  public static Shaders instance;

  private int uProjLocation = -1;
  private int uViewLocation = -1;
  private int uModelLocation = -1;
  private int uTexLocation = -1;

  private static int shaderProgram = 0;
  private static int vertShader = 0;
  private static int fragShader = 0;

  public static void create() {
    if(instance != null) {
      throw new IllegalStateException("Shaders already created");
    }
    instance = new Shaders();
  }

  public static void use() {
    if(instance == null) {
      throw new IllegalStateException("Shaders not created");
    }
    glUseProgram(shaderProgram);
  }

  private Shaders() {
    loadShaders();
  }

  private void loadShaders() {
    String vertSource = loadShaderSource("/shaders/simple.vert");
    String fragSource = loadShaderSource("/shaders/simple.frag");

    vertShader = compileShader(GL_VERTEX_SHADER, vertSource);
    fragShader = compileShader(GL_FRAGMENT_SHADER, fragSource);

    shaderProgram = glCreateProgram();
    glAttachShader(shaderProgram, vertShader);
    glAttachShader(shaderProgram, fragShader);
    glLinkProgram(shaderProgram);

    if(glGetProgrami(shaderProgram, GL_LINK_STATUS) == GL_FALSE) {
      String log = glGetProgramInfoLog(shaderProgram);
      glDeleteProgram(shaderProgram);
      glDeleteShader(vertShader);
      glDeleteShader(fragShader);
      throw new RuntimeException("Err: Shader program failed to link " + log);
    }

    glDeleteShader(vertShader);
    glDeleteShader(fragShader);

    cacheUniformLocations();
  }

  private String loadShaderSource(String path) {
    try {
      ByteBuffer fileData = readResourceToByteBuffer(path);
      byte[] bytes = new byte[fileData.remaining()];
      fileData.get(bytes);
      MemoryUtil.memFree(fileData);
      return new String(bytes);
    } catch (Exception e) {
      throw new RuntimeException("Err: Failed to load shader: " + path, e);
    }
  }

  private ByteBuffer readResourceToByteBuffer(String path) throws IOException {
    try(java.io.InputStream in = Shaders.class.getResourceAsStream(path)) {
      if(in == null) {
        throw new IOException("Err: resource not found " + path);
      }
      byte[] bytes = in.readAllBytes();
      ByteBuffer buffer = MemoryUtil.memAlloc(bytes.length);
      buffer.put(bytes);
      buffer.flip();
      return buffer;
    }
  }

  private int compileShader(int type, String source) {
    int shader = glCreateShader(type);
    glShaderSource(shader, source);
    glCompileShader(shader);

    if(glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE) {
      String log = glGetShaderInfoLog(shader);
      glDeleteShader(shader);
      String shaderType = type == GL_VERTEX_SHADER ? "vertex" : "fragment";
      throw new RuntimeException("Err: " + shaderType + " compilation failed " + log);
    }
    return shader;
  }

  private void cacheUniformLocations() {
    uProjLocation = glGetUniformLocation(shaderProgram, "uProj");
    uViewLocation = glGetUniformLocation(shaderProgram, "uView");
    uModelLocation = glGetUniformLocation(shaderProgram, "uModel");
    uTexLocation = glGetUniformLocation(shaderProgram, "uTex");

    if(uProjLocation == -1 || uViewLocation == -1 || uModelLocation == -1) {
      throw new RuntimeException("Err: Failed to cache uniforms");
    }

    if(uTexLocation == -1) {
      System.out.println("Warning: uTex not found");
    }
  }

  public static void setProjMatrix(FloatBuffer matrix) {
    if(instance == null) throw new IllegalStateException("Shaders not created");
    if(instance.uProjLocation == -1) throw new IllegalStateException("uProj uniform not found");

    glUniformMatrix4fv(instance.uProjLocation, false, matrix);
  }

  public static void setViewMatrix(FloatBuffer matrix) {
    if(instance == null) throw new IllegalStateException("Shaders not created");
    if(instance.uViewLocation == -1) throw new IllegalStateException("uView uniform not found");

    glUniformMatrix4fv(instance.uViewLocation, false, matrix);
  }

  public static void setModelMatrix(FloatBuffer matrix) {
    if(instance == null) throw new IllegalStateException("Shaders not created");
    if(instance.uModelLocation == -1) throw new IllegalStateException("uModel uniform not found");

    glUniformMatrix4fv(instance.uModelLocation, false, matrix);
  }

  public static void setTextureUnit(int unit) {
    if(instance == null) throw new IllegalStateException("Shaders not created");
    // if(instance.uTexLocation == -1) throw new IllegalStateException("uTex uniform not found");

    if(instance.uTexLocation != -1) {
      glUniform1i(instance.uTexLocation, unit);
    }
  }

  public static void cleanup() {
    glUseProgram(0);
    glDetachShader(shaderProgram, vertShader);
    glDetachShader(shaderProgram, fragShader);

    glDeleteShader(vertShader);
    glDeleteShader(fragShader);

    glDeleteProgram(shaderProgram);
  }
}
