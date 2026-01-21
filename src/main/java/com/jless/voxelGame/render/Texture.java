package com.jless.voxelGame.render;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

public class Texture {

  private final int id;
  private final int width;
  private final int height;

  public Texture(String resPath) {
    ByteBuffer image;
    int w, h;

    try(MemoryStack stack = MemoryStack.stackPush()) {
      IntBuffer x = stack.mallocInt(1);
      IntBuffer y = stack.mallocInt(1);
      IntBuffer channels = stack.mallocInt(1);

      STBImage.stbi_set_flip_vertically_on_load(true);

      ByteBuffer fileData = Resource.readToBuffer(resPath);

      image = STBImage.stbi_load_from_memory(fileData, x, y, channels, 4);
      if(image == null) {
        throw new RuntimeException("Failed to load texture: " + resPath + "\n" + STBImage.stbi_failure_reason());
      }

      w = x.get(0);
      h = y.get(0);
    }
    width = w;
    height = h;

    id = glGenTextures();
    glBindTexture(GL_TEXTURE_2D, id);

    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, image);

    glGenerateMipmap(GL_TEXTURE_2D);

    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

    STBImage.stbi_image_free(image);
    glBindTexture(GL_TEXTURE_2D, 0);
  }

  public void bind(int slot) {
    glActiveTexture(GL_TEXTURE0 + slot);
    glBindTexture(GL_TEXTURE_2D, id);
  }

  public void destroy() {
    glDeleteTextures(id);
  }

  public int width() { return width; }
  public int height() { return height; }
}
