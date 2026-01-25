package com.jless.voxelGame.chunkGen;

import java.nio.*;
import java.util.*;

import org.lwjgl.*;

import com.jless.voxelGame.*;
import com.jless.voxelGame.blocks.*;
import com.jless.voxelGame.texture.*;
import com.jless.voxelGame.worldGen.*;

public class GreedyMesher {

  public GreedyMesher() {
    this.processed = new boolean[Consts.CHUNK_SIZE][Consts.WORLD_HEIGHT][Consts.CHUNK_SIZE];
    this.faceTextures = new byte[Consts.CHUNK_SIZE][Consts.WORLD_HEIGHT][Consts.CHUNK_SIZE];
  }

  private final boolean[][][] processed;
  private final byte[][][] faceTextures;

  public FloatBuffer buildGreedyMesh(Chunk chunk, World world) {
    List<GreedyQuad> quads = new ArrayList<>();

    for(byte dir = 0; dir < 6; dir++) {
      scanPlaneForQuads(chunk, world, dir, quads);
    }
    return generateVerticesFromQuads(quads);
  }

  private void scanPlaneForQuads(Chunk chunk, World world, byte dir, List<GreedyQuad> quads) {
    for(int y = 0; y < Consts.WORLD_HEIGHT; y++) {
      for(int x = 0; x < Consts.CHUNK_SIZE; x++) {
        for(int z = 0; z < Consts.CHUNK_SIZE; z++) {
          if(processed[x][y][z] || !VoxelCuller.isFaceVisible(chunk, world, x, y, z, dir)) continue;

          GreedyQuad quad = findLargestQuad(chunk, world, x, y, z, dir);
          quads.add(quad);

          markQuadProcessed(quad);
        }
      }
    }
  }

  private GreedyQuad findLargestQuad(Chunk chunk, World world, int sX, int sY, int sZ, byte dir) {
    int texture = getFaceTexture(chunk, world, sX, sY, sZ, dir);

    int maxX = sX;
    while(maxX + 1 < Consts.CHUNK_SIZE && canMerge(chunk, world, maxX + 1, sY, sZ, dir, texture)) maxX++;

    int maxZ = sZ;
    expandLoop:
    while(maxZ + 1 < Consts.CHUNK_SIZE) {
      for(int x = sX; x <= maxX; x++) {
        if(!canMerge(chunk, world, x, sY, maxZ + 1, dir, texture)) {
          break expandLoop;
        }
      }
      maxZ++;
    }
    return new GreedyQuad(sX, sZ, maxX, maxZ, sY, texture, dir);
  }

  private void markQuadProcessed(GreedyQuad quad) {
    for(int x = quad.x1; x <= quad.x2; x++) {
      for(int z = quad.z1; z <= quad.z2; z++) {
        processed[x][quad.y][z] = true;
      }
    }
  }

  private FloatBuffer generateVerticesFromQuads(List<GreedyQuad> quads) {
    FloatBuffer buffer = BufferUtils.createFloatBuffer(800_000);
    for(GreedyQuad quad : quads) {
      emitQuadToBuffer(buffer, quad);
    }
    buffer.flip();
    return buffer;
  }

  private void emitQuadToBuffer(FloatBuffer buffer, GreedyQuad quad) {
    float width = quad.x2 - quad.x1 + 1;
    float height = quad.z2 - quad.z1 + 1;

    float[] uv = new float[4];
    getUVPacked(quad.tex, uv);

    float u0 = uv[0];
    float v0 = uv[1];
    float u1 = uv[2];
    float v1 = uv[3];

    emitQuadFaces(buffer, quad, u0, v0, u1, v1);
  }

  private void emitQuadFaces(FloatBuffer buffer, GreedyQuad quad, float u0, float v0, float u1, float v1) {
    int worldOffsetX = 0;
    int worldOffsetZ = 0;

    int x0 = quad.x1 + worldOffsetX;
    int x1 = quad.x2 + 1 + worldOffsetX;
    int z0 = quad.z1 + worldOffsetZ;
    int z1 = quad.z2 + 1 + worldOffsetZ;
    int y = quad.y;

    switch(quad.dir) {
      case 0 -> emitXPPlane(buffer, x1, y, z0, z1, u0, v0, u1, v1);
      case 1 -> emitXMPlane(buffer, x0, y, z0, z1, u0, v0, u1, v1);
      case 2 -> emitYPPlane(buffer, x0, x1, z0, z1, y, u0, v0, u1, v1);
      case 3 -> emitYMPlane(buffer, x0, x1, z0, z1, y, u0, v0, u1, v1);
      case 4 -> emitZPPlane(buffer, x0, x1, y, z0, u0, v0, u1, v1);
      case 5 -> emitZMPlane(buffer, x0, x1, y, z0, u0, v0, u1, v1);
    }
  }

  private void emitXPPlane(FloatBuffer buffer, int x, int y, int z0, int z1, float u0, float v0, float u1, float v1) {
    putV(buffer, x, y, z0, 1, 0, 0, u1, v0);
    putV(buffer, x, y + 1, z0, 1, 0, 0, u1, v1);
    putV(buffer, x, y + 1, z1, 1, 0, 0, u0, v1);
    putV(buffer, x, y, z0, 1, 0, 0, u1, v0);
    putV(buffer, x, y + 1, z1, 1, 0, 0, u0, v1);
    putV(buffer, x, y, z0, 1, 0, 0, u0, v0);
  }

  private void emitXMPlane(FloatBuffer buffer, int x, int y, int z0, int z1, float u0, float v0, float u1, float v1) {
    putV(buffer, x, y, z1, -1, 0, 0, u1, v0);
    putV(buffer, x, y + 1, z1, -1, 0, 0, u1, v1);
    putV(buffer, x, y + 1, z0, 1, 0, 0, u0, v1);
    putV(buffer, x, y, z1, 1, 0, 0, u1, v0);
    putV(buffer, x, y + 1, z0, 1, 0, 0, u0, v1);
    putV(buffer, x, y, z0, 1, 0, 0, u0, v0);
  }

  private void emitYPPlane(FloatBuffer buffer, int x0, int x1, int z0, int z1, int y, float u0, float v0, float u1, float v1) {
    putV(buffer, x0, y, z1, 0, 1, 0, u0, v0);
    putV(buffer, x1, y, z1, 0, 1, 0, u1, v0);
    putV(buffer, x1, y, z0, 0, 1, 0, u1, v1);
    putV(buffer, x0, y, z1, 0, 1, 0, u0, v0);
    putV(buffer, x1, y, z0, 0, 1, 0, u1, v1);
    putV(buffer, x0, y, z0, 0, 1, 0, u0, v1);
  }

  private void emitYMPlane(FloatBuffer buffer, int x0, int x1, int z0, int z1, int y, float u0, float v0, float u1, float v1) {
    putV(buffer, x0, y, z0, 0, -1, 0, u0, v0);
    putV(buffer, x1, y, z0, 0, -1, 0, u1, v0);
    putV(buffer, x1, y, z1, 0, -1, 0, u1, v1);
    putV(buffer, x0, y, z0, 0, -1, 0, u0, v0);
    putV(buffer, x1, y, z1, 0, -1, 0, u1, v1);
    putV(buffer, x0, y, z1, 0, -1, 0, u0, v1);
  }

  private void emitZPPlane(FloatBuffer buffer, int x0, int x1, int y, int z1, float u0, float v0, float u1, float v1) {
    putV(buffer, x1, y, z1, 0, 0, 1, u1, v1);
    putV(buffer, x1, y + 1, z1, 0, 0, 1, u1, v0);
    putV(buffer, x0, y + 1, z1, 0, 0, 1, u0, v1);
    putV(buffer, x1, y, z1, 0, 0, 1, u1, v0);
    putV(buffer, x0, y + 1, z1, 0, 0, 1, u0, v1);
    putV(buffer, x0, y, z1, 0, 0, 1, u0, v0);
  }

  private void emitZMPlane(FloatBuffer buffer, int x0, int x1, int y, int z0, float u0, float v0, float u1, float v1) {
    putV(buffer, x0, y, z0, 0, 0, -1, u1, v1);
    putV(buffer, x0, y + 1, z0, 0, 0, -1, u1, v0);
    putV(buffer, x1, y + 1, z0, 0, 0, -1, u0, v1);
    putV(buffer, x0, y, z0, 0, 0, -1, u1, v0);
    putV(buffer, x1, y + 1, z0, 0, 0, -1, u0, v1);
    putV(buffer, x1, y, z0, 0, 0, -1, u0, v0);
  }

  private void putV(FloatBuffer buffer, float px, float py, float pz, float nx, float ny, float nz, float u, float v) {
    buffer.put(px).put(py).put(pz);
    buffer.put(nx).put(ny).put(nz);
    buffer.put(u).put(v);
  }

  private void getUVPacked(int packed, float[] out) {
    int tx = TextureAtlas.tileX(packed);
    int ty = TextureAtlas.tileY(packed);

    float sx = 1.0f / TextureAtlas.ATLAS_TILE_X;
    float sy = 1.0f / TextureAtlas.ATLAS_TILE_Y;

    out[0] = tx * sx;
    out[1] = ty * sy;
    out[2] = out[0] + sx;
    out[3] = out[1] + sy;
  }

  private boolean canMerge(Chunk chunk, World world, int x, int y, int z, byte dir, int tex) {
    if(x < 0 || x >= Consts.CHUNK_SIZE || z < 0 || z >= Consts.CHUNK_SIZE) return false;
    if(processed[x][y][z]) return false;
    if(!VoxelCuller.isFaceVisible(chunk, world, x, y, z, dir)) return false;

    return getFaceTexture(chunk, world, x, y, z, dir) == tex;
  }

  private int getFaceTexture(Chunk chunk, World world, int x, int y, int z, byte dir) {
    byte blockID = chunk.get(x, y, z);

    return switch(dir) {
      case 1 -> Blocks.TEX_FRONT[blockID & 0xFF];
      case 2 -> Blocks.TEX_TOP[blockID & 0xFF];
      case 3 -> Blocks.TEX_BOTTOM[blockID & 0xFF];
      default -> Blocks.TEX_SIDE[blockID & 0xFF];
    };
  }

  private static class GreedyQuad {
	  int x1, z1, x2, z2;
    int y;
    int tex;
    byte dir;
    public GreedyQuad(int sX, int sZ, int maxX, int maxZ, int sY, int texture, byte dir) {
      this.x1 = sX;
      this.z1 = sZ;
      this.x2 = maxX;
      this.z2 = maxZ;
      this.y = sY;
      this.tex = texture;
      this.dir = dir;
    }
  }

}
