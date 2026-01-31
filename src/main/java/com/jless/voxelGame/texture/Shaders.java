package com.jless.voxelGame.texture;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

import java.io.*;
import java.nio.*;

import org.lwjgl.*;
import org.lwjgl.system.*;

import org.joml.*;

public class Shaders {

  public static Shaders instance;

  private static int uProjLocation = -1;
  private static int uViewLocation = -1;
  private static int uModelLocation = -1;
  private static int uTexLocation = -1;
  private static int uSunDirLocation = -1;
  private static int uLightColorLocation = -1;
  private static int uAmbientColorLocation = -1;
  private static int uShadowMapLocation = -1;
  private static int uShadowsEnabledLocation = -1;
  private static int uLightSpaceLocation = -1;
  private static int uFogColorLocation = -1;
  private static int uFogStartLocation = -1;
  private static int uFogEndLocation = -1;
  private static int uCameraPosLocation = -1;
  private static int uUseSolidColorLocation = -1;
  private static int uSolidColorLocation = -1;

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

  public static void cacheLightingUniforms() {
    uLightSpaceLocation = glGetUniformLocation(shaderProgram, "uLightSpace");
    uSunDirLocation = glGetUniformLocation(shaderProgram, "uSunDir");
    uLightColorLocation = glGetUniformLocation(shaderProgram, "uLightColor");
    uAmbientColorLocation = glGetUniformLocation(shaderProgram, "uAmbientColor");
    uShadowMapLocation = glGetUniformLocation(shaderProgram, "uShadowMap");
    uShadowsEnabledLocation = glGetUniformLocation(shaderProgram, "uShadowsEnabled");
  }

  private void cacheUniformLocations() {
    uProjLocation = glGetUniformLocation(shaderProgram, "uProj");
    uViewLocation = glGetUniformLocation(shaderProgram, "uView");
    uModelLocation = glGetUniformLocation(shaderProgram, "uModel");
    uTexLocation = glGetUniformLocation(shaderProgram, "uTex");
    uFogStartLocation = glGetUniformLocation(shaderProgram, "uFogStart");
    uFogEndLocation = glGetUniformLocation(shaderProgram, "uFogEnd");
    uFogColorLocation = glGetUniformLocation(shaderProgram, "uFogColor");
    uCameraPosLocation = glGetUniformLocation(shaderProgram, "uCameraPos");
    uUseSolidColorLocation = glGetUniformLocation(shaderProgram, "useSolidColor");
    uSolidColorLocation = glGetUniformLocation(shaderProgram, "SolidColor");

    if(uProjLocation == -1 || uViewLocation == -1 || uModelLocation == -1) {
      throw new RuntimeException("Err: Failed to cache uniforms");
    }

    if(uTexLocation == -1) {
      System.out.println("Warning: uTex not found");
    }

    if(uUseSolidColorLocation != -1) {
      glUseProgram(shaderProgram);
      glUniform1i(uUseSolidColorLocation, 0);
      System.out.println("Info: solid color uniforms cached");
    } else {
      System.out.println("Err: Solid uniforms not cached");
    }
  }

  public static void setUniformInt(String name, int value) {
    int location = glGetUniformLocation(shaderProgram, name);
    if(location != -1) {
      glUniform1i(location, value);
    }
  }

  public static void setUniformVec(String name, Vector3f vec) {
    int location = glGetUniformLocation(shaderProgram, name);
    if(location != -1) {
      glUniform3f(location, vec.x, vec.y, vec.z);
    }
  }

  public static void setUseSolidColor(int enabled) {
    if(uUseSolidColorLocation != -1) {
      glUniform1i(uUseSolidColorLocation, enabled);
    }
  }

  public static void setSolidColor(Vector3f color) {
    if(uSolidColorLocation != -1) {
      glUniform3f(uSolidColorLocation, color.x, color.y, color.z);
    }
  }

  public static void setLightSpaceMatrix(Matrix4f matrix) {
    if(uLightSpaceLocation >= 0) {
      FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
      matrix.get(buffer);
      glUniformMatrix4fv(uLightSpaceLocation, false, buffer);
    }
  }

  public static void setSunDir(Vector3f dir) {
    if(uSunDirLocation >= 0) {
      glUniform3f(uSunDirLocation, dir.x, dir.y, dir.z);
    }
  }

  public static void setLightColor(Vector3f color) {
    if(uLightColorLocation >= 0) {
      glUniform3f(uLightColorLocation, color.x, color.y, color.z);
    }
  }

  public static void setAmbientColor(Vector3f color) {
    if(uAmbientColorLocation >= 0) {
      glUniform3f(uAmbientColorLocation, color.x, color.y, color.z);
    }
  }

  public static void setShadowMap(int texUnit) {
    if(uShadowMapLocation >= 0) {
      glUniform1i(uShadowMapLocation, texUnit);
    }
  }

  public static void setShadowEnabled(boolean enabled) {
    if(uShadowsEnabledLocation >= 0) {
      glUniform1i(uShadowsEnabledLocation, enabled ? 1 : 0);
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
    if(instance.uTexLocation == -1) throw new IllegalStateException("uTex uniform not found");

    if(instance.uTexLocation != -1) {
      glUniform1i(instance.uTexLocation, unit);
    }
  }

  public static void setFogParams(float r, float g, float b, float start, float end) {
    if(instance == null) throw new IllegalStateException("Shaders not created");
    if(instance.uFogColorLocation != -1) {
      glUniform3f(instance.uFogColorLocation, r, g, b);
    }
    if(instance.uFogStartLocation != -1) {
      glUniform1f(instance.uFogStartLocation, start);
    }
    if(instance.uFogEndLocation != -1) {
      glUniform1f(instance.uFogEndLocation, end);
    }
  }

  public static void setCameraPos(Vector3f pos) {
    if(instance == null) throw new IllegalStateException("Shaders not created");
    if(instance.uCameraPosLocation != -1) {
      glUniform3f(instance.uCameraPosLocation, pos.x, pos.y, pos.z);
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
