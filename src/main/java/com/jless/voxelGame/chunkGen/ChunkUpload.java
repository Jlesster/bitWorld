package com.jless.voxelGame.chunkGen;

import java.nio.*;

public class ChunkUpload {
  public final Chunk chunk;
  public final FloatBuffer meshData;
  final long completionTime;

  public ChunkUpload(Chunk chunk, FloatBuffer meshData) {
    this.chunk = chunk;
    this.meshData = meshData;
    this.completionTime = System.currentTimeMillis();
  }
}
