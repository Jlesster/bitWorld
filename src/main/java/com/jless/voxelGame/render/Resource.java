package com.jless.voxelGame.render;

import java.io.InputStream;
import java.nio.ByteBuffer;

import org.lwjgl.BufferUtils;

public class Resource {

  public static ByteBuffer readToBuffer(String path) {
    try {
      InputStream in = Resource.class.getClassLoader().getResourceAsStream(path);
      if(in == null) throw new RuntimeException("Resource not found: " + path);

      byte[] bytes = in.readAllBytes();
      ByteBuffer buffer = BufferUtils.createByteBuffer(bytes.length);
      buffer.put(bytes);
      buffer.flip();
      return buffer;
    } catch(Exception e) {
      throw new RuntimeException("Failed reading resource: " + path, e);
    }
  }
}
