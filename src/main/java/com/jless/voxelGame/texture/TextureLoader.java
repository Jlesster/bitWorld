package com.jless.voxelGame.texture;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL30.*;

import java.awt.image.*;
import java.io.*;
import java.nio.*;

import javax.imageio.*;

import org.lwjgl.*;
import org.lwjgl.stb.*;
import org.lwjgl.system.*;

public final class TextureLoader {

  public final int id;
  public final int width;
  public final int height;

  private TextureLoader(int id, int w, int h) {
    this.id = id;
    this.width = w;
    this.height = h;
  }

  public static TextureLoader load(String filePath, boolean flipY) {
    STBImage.stbi_set_flip_vertically_on_load(flipY);

    IntBuffer w = BufferUtils.createIntBuffer(1);
    IntBuffer h = BufferUtils.createIntBuffer(1);
    IntBuffer comp = BufferUtils.createIntBuffer(1);

    ByteBuffer pixels = STBImage.stbi_load(filePath, w, h, comp, 4);
    if(pixels == null) {
      throw new RuntimeException("Err: Failed to load tex (" + filePath + ", STBReason: " + STBImage.stbi_failure_reason() + ")");
    }

    int width = w.get(0);
    int height = h.get(0);

    int texID = glGenTextures();
    glBindTexture(GL_TEXTURE_2D, texID);

    glTexImage2D(
      GL_TEXTURE_2D,
      0,
      GL_RGBA8,
      width,
      height,
      0,
      GL_RGBA,
      GL_UNSIGNED_BYTE,
      pixels
    );

    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

    STBImage.stbi_image_free(pixels);
    glBindTexture(GL_TEXTURE_2D, 0);

    return new TextureLoader(texID, width, height);
  }

  public static int loadTexArray(String path, int tileSize) {
    BufferedImage image;

    try(InputStream in = TextureLoader.class.getResourceAsStream(path)) {
      if(in == null) {
        throw new RuntimeException("Texture array source not found");
      }
      image = ImageIO.read(in);
    } catch (IOException e) {

      throw new RuntimeException("Failed to load tex array: " + path, e);
    }

    int atlasWidth = image.getWidth();
    int atlasHeight = image.getHeight();

    int tilesX = atlasWidth / tileSize;
    int tilesY = atlasHeight / tileSize;
    int layerCount = tilesX * tilesY;

    ByteBuffer buffer = BufferUtils.createByteBuffer(tileSize * tileSize * 4 * layerCount);

    for(int ty = 0; ty < tilesY; ty++) {
      for(int tx = 0; tx < tilesX; tx++) {

        for(int y = 0; y < tileSize; y++) {
          for(int x = 0; x < tileSize; x++) {
            int px = tx * tileSize + x;
            int py = ty * tileSize + y;
            int argb = image.getRGB(px, py);

            buffer.put((byte)((argb >> 16) & 0xFF));
            buffer.put((byte)((argb >> 8) & 0xFF));
            buffer.put((byte)(argb & 0xFF));
            buffer.put((byte)((argb >> 24) & 0xFF));
          }
        }
      }
    }
    buffer.flip();

    int texID = glGenTextures();
    glBindTexture(GL_TEXTURE_2D_ARRAY, texID);
    glTexImage3D(GL_TEXTURE_2D_ARRAY, 0, GL_RGBA8, tileSize, tileSize, layerCount, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);

    glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
    glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
    glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_WRAP_S, GL_REPEAT);
    glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_WRAP_T, GL_REPEAT);
    glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_BASE_LEVEL, 0);
    glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MAX_LEVEL, 0);

    glBindTexture(GL_TEXTURE_2D_ARRAY, 0);

    return texID;
  }

  public static TextureLoader loadResource(String cp, boolean flipY) {
    if(!cp.startsWith("/")) {
      throw new RuntimeException("Resource path must start with /");
    }

    STBImage.stbi_set_flip_vertically_on_load(flipY);
    ByteBuffer fileData = null;
    ByteBuffer pixels = null;

    IntBuffer w = BufferUtils.createIntBuffer(1);
    IntBuffer h = BufferUtils.createIntBuffer(1);
    IntBuffer comp = BufferUtils.createIntBuffer(1);

    try {
      fileData = readResourceToByteBuffer(cp);

      pixels = STBImage.stbi_load_from_memory(fileData, w, h, comp, 4);
      if(pixels == null) {
        throw new RuntimeException("Failed to load texture resource");
      }
      TextureLoader tex = uploadRGBA(pixels, w.get(0), h.get(0));
      STBImage.stbi_image_free(pixels);
      return tex;
    } catch (IOException e) {
      throw new RuntimeException("Failed reading resource");
    } finally {
      if(fileData != null) MemoryUtil.memFree(fileData);
    }
  }

  private static TextureLoader uploadRGBA(ByteBuffer pixels, int w, int h) {
    int texID = glGenTextures();

    glTexImage2D(
      GL_TEXTURE_2D,
      0,
      GL_RGBA8,
      w,
      h,
      0,
      GL_RGBA,
      GL_UNSIGNED_BYTE,
      pixels
    );

    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

    glBindTexture(GL_TEXTURE_2D, 0);

    return new TextureLoader(texID, w, h);
  }

  private static ByteBuffer readResourceToByteBuffer(String path) throws IOException {
    try(InputStream in = TextureLoader.class.getResourceAsStream(path)) {
      if(in == null) {
        throw new IOException("Err Resource not found: " + path);
      }
      byte[] bytes = in.readAllBytes();
      ByteBuffer buffer = MemoryUtil.memAlloc(bytes.length);
      buffer.put(bytes);
      buffer.flip();
      return buffer;
    }
  }

  public void bind() {
    glBindTexture(GL_TEXTURE_2D, id);
  }

  public void unbind() {
    glBindTexture(GL_TEXTURE_2D, 0);
  }

  public void cleanup() {
    glDeleteTextures(id);
  }
}
