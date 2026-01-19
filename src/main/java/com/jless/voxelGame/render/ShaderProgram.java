package com.jless.voxelGame.render;

import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL20.*;

import java.io.*;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

import org.lwjgl.system.MemoryStack;

import org.joml.Matrix4f;

public class ShaderProgram {

  private final int programID;
  private final Map<String, Integer> uniformCache = new HashMap<>();

  public ShaderProgram(String vertPath, String fragPath) {
    int vertID = compileShader(vertPath, GL_VERTEX_SHADER);
    int fragID = compileShader(fragPath, GL_FRAGMENT_SHADER);

    programID = glCreateProgram();
    glAttachShader(programID, vertID);
    glAttachShader(programID, fragID);

    glLinkProgram(programID);
    if(glGetProgrami(programID, GL_LINK_STATUS) == GL_FALSE) {
      throw new RuntimeException("Shader link failed:\n" + glGetProgramInfoLog(programID));
    }

    glValidateProgram(programID);

    glDetachShader(programID, vertID);
    glDetachShader(programID, fragID);
    glDeleteShader(vertID);
    glDeleteShader(fragID);
  }

  private int compileShader(String resPath, int type) {
    String src = readResource(resPath);

    int shaderID = glCreateShader(type);

    glShaderSource(shaderID, src);
    glCompileShader(shaderID);

    if(glGetShaderi(shaderID, GL_COMPILE_STATUS) == GL_FALSE) {
      throw new RuntimeException("Shader compile failed (" + resPath + "):\n" + glGetShaderInfoLog(shaderID));
    }
    return shaderID;
  }

  private String readResource(String path) {
    try {
      InputStream in = ShaderProgram.class.getClassLoader().getResourceAsStream(path);
      if(in == null) throw new RuntimeException("Shader not found: " + path);

      StringBuilder sb = new StringBuilder();
      BufferedReader br = new BufferedReader(new InputStreamReader(in));

      String line;
      while((line = br.readLine()) != null) sb.append(line).append("\n");
      br.close();

      return sb.toString();
    } catch (Exception e) {
      throw new RuntimeException("Failed reading shader: " + path, e);
    }
  }

  public void bind() {
    glUseProgram(programID);
  }

  public void unbind() {
    glUseProgram(0);
  }

  public void destory() {
    glDeleteProgram(programID);
  }

  private int uniform(String name) {
    Integer loc = uniformCache.get(name);
    if(loc != null) return loc;

    int location = glGetUniformLocation(programID, name);
    if(location == -1) {
      System.err.println("WARNING: uniform not found: " + name);
    }
    uniformCache.put(name, location);
    return location;
  }

  public void setMat4(String name, Matrix4f mat) {
    try(MemoryStack stack = MemoryStack.stackPush()) {
      FloatBuffer fb = stack.mallocFloat(16);
      mat.get(fb);
      glUniformMatrix4fv(uniform(name), false, fb);
    }
  }

  public void setInt(String name, int value) {
    glUniform1i(uniform(name), value);
  }
}
