package com.jless.voxelGame.chunkGen;

import java.nio.*;
import java.util.*;

import org.lwjgl.*;

import com.jless.voxelGame.*;
import com.jless.voxelGame.blocks.*;
import com.jless.voxelGame.texture.*;
import com.jless.voxelGame.worldGen.*;

public class GreedyMesher {

  private final boolean[][][] processed;

  public GreedyMesher() {
    this.processed = new boolean[Consts.CHUNK_SIZE][Consts.WORLD_HEIGHT][Consts.CHUNK_SIZE];
  }

  private void resetProcessedArrays() {
    for(int x = 0; x < Consts.CHUNK_SIZE; x++) {
      for(int y = 0; y < Consts.WORLD_HEIGHT; y++) {
        for(int z = 0; z < Consts.CHUNK_SIZE; z++) {
          processed[x][y][z] = false;
        }
      }
    }
  }

  public FloatBuffer buildGreedyMesh(Chunk chunk, World world) {
    List<GreedyQuad> quads = new ArrayList<>();

    resetProcessedArrays();

    for(byte dir = 0; dir < 6; dir++) {
      resetProcessedArrays();
      scanPlaneForQuads(chunk, world, dir, quads);
    }
    return generateVerticesFromQuads(quads);
  }

  private void scanPlaneForQuads(Chunk chunk, World world, byte dir, List<GreedyQuad> quads) {
    if(dir == 0 || dir == 1) {
      for(int x = 0; x < Consts.CHUNK_SIZE; x++) {
        for(int y = 0; y < Consts.WORLD_HEIGHT; y++) {
          for(int z = 0; z < Consts.CHUNK_SIZE; z++) {
            if(processed[x][y][z] || !VoxelCuller.isFaceVisible(chunk, world, x, y, z, dir)) continue;
            GreedyQuad quad = findLargestQuadYZ(chunk, world, x, y, z, dir);
            quads.add(quad);
            markQuadProcessed(quad, dir);
          }
        }
      }
    } else if(dir == 2 || dir == 3) {
      for(int y = 0; y < Consts.WORLD_HEIGHT; y++) {
        for(int x = 0; x < Consts.CHUNK_SIZE; x++) {
          for(int z = 0; z < Consts.CHUNK_SIZE; z++) {
            if(processed[x][y][z] || !VoxelCuller.isFaceVisible(chunk, world, x, y, z, dir)) continue;
            GreedyQuad quad = findLargestQuadXZ(chunk, world, x, y, z, dir);
            quads.add(quad);
            markQuadProcessed(quad, dir);
          }
        }
      }
    } else {
      for(int z = 0; z < Consts.CHUNK_SIZE; z++) {
        for(int y = 0; y < Consts.WORLD_HEIGHT; y++) {
          for(int x = 0; x < Consts.CHUNK_SIZE; x++) {
            if(processed[x][y][z] || !VoxelCuller.isFaceVisible(chunk, world, x, y, z, dir)) continue;
            GreedyQuad quad = findLargestQuadXY(chunk, world, x, y, z, dir);
            quads.add(quad);
            markQuadProcessed(quad, dir);
          }
        }
      }
    }
  }

  private GreedyQuad findLargestQuadXY(Chunk chunk, World world, int x, int y, int z, byte dir) {
    int texture = getFaceTexture(chunk, world, x, y, z, dir);

    int maxX = x;
    while(maxX + 1 < Consts.CHUNK_SIZE && canMerge(chunk, world, maxX + 1, y, z, dir, texture)) maxX++;

    int maxY = y;
    expandLoop:
    while(maxY + 1 < Consts.WORLD_HEIGHT) {
      for(int xx = x; xx <= maxX; xx++) {
        if(!canMerge(chunk, world, xx, maxY + 1, z, dir, texture)) {
          break expandLoop;
        }
      }
      maxY++;
    }
    return new GreedyQuad(x, y, z, maxX, maxY, z, texture, dir);
  }

  private GreedyQuad findLargestQuadXZ(Chunk chunk, World world, int x, int y, int z, byte dir) {
    int texture = getFaceTexture(chunk, world, x, y, z, dir);

    int maxX = x;
    while(maxX + 1 < Consts.CHUNK_SIZE && canMerge(chunk, world, maxX + 1, y, z, dir, texture)) maxX++;

    int maxZ = z;
    expandLoop:
    while(maxZ + 1 < Consts.CHUNK_SIZE) {
      for(int xx = x; xx <= maxX; xx++) {
        if(!canMerge(chunk, world, xx, y, maxZ + 1, dir, texture)) {
          break expandLoop;
        }
      }
      maxZ++;
    }
    return new GreedyQuad(x, y, z, maxX, y, maxZ, texture, dir);
  }

  private GreedyQuad findLargestQuadYZ(Chunk chunk, World world, int x, int y, int z, byte dir) {
    int texture = getFaceTexture(chunk, world, x, y, z, dir);

    int maxZ = z;
    while(maxZ + 1 < Consts.CHUNK_SIZE && canMerge(chunk, world, x, y, maxZ + 1, dir, texture)) maxZ++;

    int maxY = y;
    expandLoop:
    while(maxY + 1 < Consts.WORLD_HEIGHT) {
      for(int zz = z; zz <= maxZ; zz++) {
        if(!canMerge(chunk, world, x, maxY + 1, zz, dir, texture)) {
          break expandLoop;
        }
      }
      maxY++;
    }
    return new GreedyQuad(x, y, z, x, maxY, maxZ, texture, dir);
  }

  private void markQuadProcessed(GreedyQuad quad, byte dir) {
    if(dir == 0 || dir == 1) {
      for(int y = quad.y1; y <= quad.y2; y++) {
        for(int z = quad.z1; z <= quad.z2; z++) {
          processed[quad.x1][y][z] = true;
        }
      }
    } else if(dir == 2 || dir == 3) {
      for(int x = quad.x1; x <= quad.x2; x++) {
        for(int z = quad.z1; z <= quad.z2; z++) {
          processed[x][quad.y1][z] = true;
        }
      }
    } else {
      for(int x = quad.x1; x <= quad.x2; x++) {
        for(int y = quad.y1; y <= quad.y2; y++) {
          processed[x][y][quad.z1] = true;
        }
      }
    }
  }

  private FloatBuffer generateVerticesFromQuads(List<GreedyQuad> quads) {
    FloatBuffer buffer = BufferUtils.createFloatBuffer(2_000_000);
    for(GreedyQuad quad : quads) {
      emitQuadToBuffer(buffer, quad);
    }
    buffer.flip();
    return buffer;
  }

  private void emitQuadToBuffer(FloatBuffer buffer, GreedyQuad quad) {
    float[] uv = new float[4];
    getUVPacked(quad.tex, uv);

    float u0 = uv[0];
    float v0 = uv[1];
    float u1 = uv[2];
    float v1 = uv[3];

    emitQuadFaces(buffer, quad, u0, v0, u1, v1);
  }

  private void emitQuadFaces(FloatBuffer buffer, GreedyQuad quad, float u0, float v0, float u1, float v1) {
    switch(quad.dir) {
      case 0 -> emitXPPlane(buffer, quad, u0, v0, u1, v1);
      case 1 -> emitXMPlane(buffer, quad, u0, v0, u1, v1);
      case 2 -> emitYPPlane(buffer, quad, u0, v0, u1, v1);
      case 3 -> emitYMPlane(buffer, quad, u0, v0, u1, v1);
      case 4 -> emitZPPlane(buffer, quad, u0, v0, u1, v1);
      case 5 -> emitZMPlane(buffer, quad, u0, v0, u1, v1);
    }
  }

  private void emitXPPlane(FloatBuffer buffer, GreedyQuad q, float u0, float v0, float u1, float v1) {
    float x = q.x2 + 1;
    float y0 = q.y1;
    float y1 = q.y2 + 1;
    float z0 = q.z1;
    float z1 = q.z2 + 1;

    putV(buffer, x, y0, z1, 1, 0, 0, u0, v0);
    putV(buffer, x, y1, z1, 1, 0, 0, u0, v1);
    putV(buffer, x, y1, z0, 1, 0, 0, u1, v1);

    putV(buffer, x, y0, z1, 1, 0, 0, u0, v0);
    putV(buffer, x, y1, z0, 1, 0, 0, u1, v1);
    putV(buffer, x, y0, z0, 1, 0, 0, u1, v0);
  }

  private void emitXMPlane(FloatBuffer buffer, GreedyQuad q, float u0, float v0, float u1, float v1) {
    float x = q.x1;
    float y0 = q.y1;
    float y1 = q.y2 + 1;
    float z0 = q.z1;
    float z1 = q.z2 + 1;

    putV(buffer, x, y0, z0, -1, 0, 0, u0, v0);
    putV(buffer, x, y1, z0, -1, 0, 0, u0, v1);
    putV(buffer, x, y1, z1, -1, 0, 0, u1, v1);

    putV(buffer, x, y0, z0, -1, 0, 0, u0, v0);
    putV(buffer, x, y1, z1, -1, 0, 0, u1, v1);
    putV(buffer, x, y0, z1, -1, 0, 0, u1, v0);
  }

  private void emitYPPlane(FloatBuffer buffer, GreedyQuad q, float u0, float v0, float u1, float v1) {
    float x0 = q.x1;
    float x1 = q.x2 + 1;
    float y = q.y2 + 1;
    float z0 = q.z1;
    float z1 = q.z2 + 1;

    putV(buffer, x0, y, z1, 0, 1, 0, u0, v0);
    putV(buffer, x0, y, z0, 0, 1, 0, u0, v1);
    putV(buffer, x1, y, z0, 0, 1, 0, u1, v1);

    putV(buffer, x0, y, z1, 0, 1, 0, u0, v0);
    putV(buffer, x1, y, z0, 0, 1, 0, u1, v1);
    putV(buffer, x1, y, z1, 0, 1, 0, u1, v0);
  }

  private void emitYMPlane(FloatBuffer buffer, GreedyQuad q, float u0, float v0, float u1, float v1) {
    float x0 = q.x1;
    float x1 = q.x2 + 1;
    float y = q.y1;
    float z0 = q.z1;
    float z1 = q.z2 + 1;

    putV(buffer, x0, y, z0, 0, -1, 0, u0, v0);
    putV(buffer, x0, y, z1, 0, -1, 0, u0, v1);
    putV(buffer, x1, y, z1, 0, -1, 0, u1, v1);

    putV(buffer, x0, y, z0, 0, -1, 0, u0, v0);
    putV(buffer, x1, y, z1, 0, -1, 0, u1, v1);
    putV(buffer, x1, y, z0, 0, -1, 0, u1, v0);
  }

  private void emitZPPlane(FloatBuffer buffer, GreedyQuad q, float u0, float v0, float u1, float v1) {
    float x0 = q.x1;
    float x1 = q.x2 + 1;
    float y0 = q.y1;
    float y1 = q.y2 + 1;
    float z = q.z2 + 1;

    putV(buffer, x1, y0, z, 0, 0, 1, u0, v0);
    putV(buffer, x1, y1, z, 0, 0, 1, u0, v1);
    putV(buffer, x0, y1, z, 0, 0, 1, u1, v1);

    putV(buffer, x1, y0, z, 0, 0, 1, u0, v0);
    putV(buffer, x0, y1, z, 0, 0, 1, u1, v1);
    putV(buffer, x0, y0, z, 0, 0, 1, u1, v0);
  }

  private void emitZMPlane(FloatBuffer buffer, GreedyQuad q, float u0, float v0, float u1, float v1) {
    float x0 = q.x1;
    float x1 = q.x2 + 1;
    float y0 = q.y1;
    float y1 = q.y2 + 1;
    float z = q.z1;

    putV(buffer, x0, y0, z, 0, 0, -1, u0, v0);
    putV(buffer, x0, y1, z, 0, 0, -1, u0, v1);
    putV(buffer, x1, y1, z, 0, 0, -1, u1, v1);

    putV(buffer, x0, y0, z, 0, 0, -1, u0, v0);
    putV(buffer, x1, y1, z, 0, 0, -1, u1, v1);
    putV(buffer, x1, y0, z, 0, 0, -1, u1, v0);
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
    if(x < 0 || x >= Consts.CHUNK_SIZE || y < 0 || y >= Consts.WORLD_HEIGHT || z < 0 || z >= Consts.CHUNK_SIZE) return false;
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
	  int x1, y1, z1, x2, y2, z2;
    int tex;
    byte dir;

    public GreedyQuad(int x1, int y1, int z1, int x2, int y2, int z2, int texture, byte dir) {
      this.x1 = x1;
      this.y1 = y1;
      this.z1 = z1;
      this.x2 = x2;
      this.y2 = y2;
      this.z2 = z2;
      this.tex = texture;
      this.dir = dir;
    }
  }

}
