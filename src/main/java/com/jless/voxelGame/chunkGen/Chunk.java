package com.jless.voxelGame.chunkGen;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.nio.*;

import org.lwjgl.*;

import org.joml.*;

import com.jless.voxelGame.*;
import com.jless.voxelGame.blocks.*;
import com.jless.voxelGame.texture.*;
import com.jless.voxelGame.worldGen.*;

public class Chunk {

  public final Vector3i pos;
  public final BlockMap blocks;

  private boolean greedyMeshingEnabled = Consts.ENABLE_GREEDY_MESHING;
  public boolean dirty = true;
  public volatile boolean uploaded = false;
  public int vertCount = 0;
  public int vboID = 0;
  public int vaoID = 0;

  public Chunk(Vector3i pos) {
    this.pos = new Vector3i(pos);
    this.blocks = new BlockMap(
      Consts.CHUNK_SIZE,
      Consts.WORLD_HEIGHT,
      Consts.CHUNK_SIZE
    );
  }

  public byte get(int x, int y, int z) {
    return blocks.get(x, y, z);
  }

  public void set(int x, int y, int z, byte id) {
    blocks.set(x, y, z, id);
    dirty = true;
  }

  public BlockMap getBlockMap() {
    return blocks;
  }

  public FloatBuffer buildMesh(World w) {
    FloatBuffer data = buildMeshBuffer(w);
    if(greedyMeshingEnabled) {
      this.vertCount = data.limit() / 8;
    }
    System.out.println("buildMesh: pos=" + pos + " dataLimit=" + data.limit() + " vertCount=" + vertCount);
    return data;
  }

  private FloatBuffer buildMeshBuffer(World w) {
    if(greedyMeshingEnabled) {
      GreedyMesher mesher = new GreedyMesher();
      return mesher.buildGreedyMesh(this, w);
    } else {
      return buildNativeMesh(w);
    }
  }

  private FloatBuffer buildNativeMesh(World w) {
    int initFloats = 4_500_000;
    FloatBuffer buf = BufferUtils.createFloatBuffer(initFloats);

    BlockMap map = getBlockMap();

    int baseX = pos.x * Consts.CHUNK_SIZE;
    int baseZ = pos.z * Consts.CHUNK_SIZE;

    int count = 0;

    try {
      for(int x = 0; x < map.sizeX(); x++) {
        for(int y = 0; y < map.sizeY(); y++) {
          for(int z = 0; z < map.sizeZ(); z++) {
            byte id = map.get(x, y, z);
            if(!Blocks.SOLID[id]) continue;

            int wx = baseX + x;
            int wz = baseZ + z;

            for(int face = 0; face < 6; face++) {
            try {
                if(VoxelCuller.isFaceVisible(w, wx, y, wz, face)) {
                  int packedTile = getTextureForFace(id, face);
                  count += emitFaceAsTriangle(buf, wx, y, wz, face, id, packedTile);
                }
              } catch(Exception e) {
                System.err.println("Error checking vace visibilty at chynk " + pos + " local(" + x + ", " + y + ", " + z + ") world(" + wx + ", " + y + ", " + wz + ") face=" + face);
                throw e;
              }
            }
          }
        }
      }
    } catch(Exception e) {
      System.err.println("Error in buildNativeMesh for chunk " + pos);
      e.printStackTrace();
      throw new RuntimeException("Mesh build failed", e);
    }

    vertCount = count;
    buf.flip();
    return buf;
  }


  public void ensureUploaded(World w) {
    if(!dirty && uploaded) return;

    FloatBuffer data = buildMesh(w);
    uploadToGPU(data);
  }

  public void uploadToGPU(FloatBuffer data) {
    if(data == null) {
      System.err.println("Err: uploadToGPU called with null data");
      return;
    }
    if(vboID == 0) vboID = glGenBuffers();
    if(vaoID == 0) vaoID = glGenVertexArrays();
    System.out.println("DEBUG: uploadToGPU: vaoID: " + vaoID + " vboID: " + vboID + " dataSize: " + data.limit());
    glBindVertexArray(vaoID);
    glBindBuffer(GL_ARRAY_BUFFER, vboID);
    glBufferData(GL_ARRAY_BUFFER, data, GL_STATIC_DRAW);

    int stride = 8 * Float.BYTES;
    glEnableVertexAttribArray(0);
    glEnableVertexAttribArray(1);
    glEnableVertexAttribArray(2);
    glVertexAttribPointer(0, 3, GL_FLOAT, false, stride, 0L);
    glVertexAttribPointer(1, 3, GL_FLOAT, false, stride, 3L * Float.BYTES);
    glVertexAttribPointer(2,2, GL_FLOAT, false, stride, 6L * Float.BYTES);
    glBindBuffer(GL_ARRAY_BUFFER, 0);
    glBindVertexArray(0);

    dirty = false;
    uploaded = true;
    System.out.println("DEBUG uploadToGPU complete: uploaded=" + uploaded);
  }

  private int getTextureForFace(byte id, int face) {
    return switch(face) {
      case 1 -> Blocks.TEX_FRONT[id];
      case 2 -> Blocks.TEX_TOP[id];
      case 3 -> Blocks.TEX_BOTTOM[id];
      default -> Blocks.TEX_SIDE[id];
    };
  }

  private int emitFaceAsTriangle(FloatBuffer b, int x, int y, int z, int face, byte id, int packedTile) {

    float[] uv = new float[4];
    getUVPacked(packedTile, uv);

    float u0 = uv[0];
    float v0 = uv[3];
    float u1 = uv[2];
    float v1 = uv[1];

    float au = 0, av = 0;
    float bu = 0, bv = 0;
    float cu = 0, cv = 0;
    float du = 0, dv = 0;

    float nx = 0, ny = 0, nz = 0;
    float x0 = x, x1 = x + 1;
    float y0 = y, y1 = y + 1;
    float z0 = z, z1 = z + 1;

    float ax = 0, ay = 0, az = 0;
    float bx = 0, by = 0, bz = 0;
    float cx = 0, cy = 0, cz = 0;
    float dx = 0, dy = 0, dz = 0;

    switch(face) {
      case 0 -> {
        nx = 1; ny = 0; nz = 0;
        ax = x1; ay = y0; az = z0;
        bx = x1; by = y1; bz = z0;
        cx = x1; cy = y1; cz = z1;
        dx = x1; dy = y0; dz = z1;

        au = u0; av = v0;
        bu = u0; bv = v1;
        cu = u1; cv = v1;
        du = u1; dv = v0;
      }
      case 1 -> {
        nx = -1; ny = 0; nz = 0;
        ax = x0; ay = y0; az = z1;
        bx = x0; by = y1; bz = z1;
        cx = x0; cy = y1; cz = z0;
        dx = x0; dy = y0; dz = z0;

        au = u1; av = v0;
        bu = u1; bv = v1;
        cu = u0; cv = v1;
        du = u0; dv = v0;
      }
      case 2 -> {
        nx = 0; ny = 1; nz = 0;
        ax = x0; ay = y1; az = z1;
        bx = x1; by = y1; bz = z1;
        cx = x1; cy = y1; cz = z0;
        dx = x0; dy = y1; dz = z0;

        au = u0; av = v1;
        bu = u1; bv = v1;
        cu = u1; cv = v0;
        du = u0; dv = v0;
      }
      case 3 -> {
        nx = 0; ny = -1; nz = 0;
        ax = x0; ay = y0; az = z0;
        bx = x1; by = y0; bz = z0;
        cx = x1; cy = y0; cz = z1;
        dx = x0; dy = y0; dz = z1;

        au = u0; av = v0;
        bu = u1; bv = v0;
        cu = u1; cv = v1;
        du = u0; dv = v1;
      }
      case 4 -> {
        nx = 0; ny = 0; nz = 1;
        ax = x1; ay = y0; az = z1;
        bx = x1; by = y1; bz = z1;
        cx = x0; cy = y1; cz = z1;
        dx = x0; dy = y0; dz = z1;

        au = u1; av = v0;
        bu = u1; bv = v1;
        cu = u0; cv = v1;
        du = u0; dv = v0;
      }
      case 5 -> {
        nx = 0; ny = 0; nz = -1;
        ax = x0; ay = y0; az = z0;
        bx = x0; by = y1; bz = z0;
        cx = x1; cy = y1; cz = z0;
        dx = x1; dy = y0; dz = z0;

        au = u0; av = v0;
        bu = u0; bv = v1;
        cu = u1; cv = v1;
        du = u1; dv = v0;
      }
    }
    putV(b, ax, ay, az, nx, ny, nz, au, av);
    putV(b, bx, by, bz, nx, ny, nz, bu, bv);
    putV(b, cx, cy, cz, nx, ny, nz, cu, cv);

    putV(b, ax, ay, az, nx, ny, nz, au, av);
    putV(b, cx, cy, cz, nx, ny, nz, cu, cv);
    putV(b, dx, dy, dz, nx, ny, nz, du, dv);

    return 6;
  }

  public void putV(FloatBuffer b,
  float px, float py, float pz,
  float nx, float ny, float nz,
  float u, float v) {
    b.put(px).put(py).put(pz);
    b.put(nx).put(ny).put(nz);
    b.put(u).put(v);
  }

  public void drawVBO() {
    if(!uploaded || vboID == 0 || vertCount == 0) return;
    glBindVertexArray(vaoID);
    glDrawArrays(GL_TRIANGLES, 0, vertCount);
    glBindVertexArray(0);

    int error = glGetError();
    if(error != GL_NO_ERROR) {
      System.err.println("OpenGL Error in drawVBO" + error);
    }
  }

  public static void getUVPacked(int packed, float[] out) {
    int tx = TextureAtlas.tileX(packed);
    int ty = TextureAtlas.tileY(packed);

    float sx = 1.0f / TextureAtlas.ATLAS_TILE_X;
    float sy = 1.0f / TextureAtlas.ATLAS_TILE_Y;

    ty = (TextureAtlas.ATLAS_TILE_Y - 1) - ty;

    float u0 = tx * sx;
    float v0 = ty * sy;
    float u1 = u0 + sx;
    float v1 = v0 + sy;

    out[0] = u0; out[1] = v0;
    out[2] = u1; out[3] = v1;
  }

  private synchronized boolean isDirty() { return dirty; }
  private synchronized boolean isUploaded() { return uploaded; }
  private synchronized void setUploaded(boolean uploaded) { this.uploaded = uploaded; }

  public synchronized void markDirty() {
    dirty = true;
  }


  public static boolean isChunkVisible(Chunk c, Vector3f playerPos) {
    float chunkX = c.pos.x * Consts.CHUNK_SIZE + Consts.CHUNK_SIZE * 0.5f;
    float chunkZ = c.pos.z * Consts.CHUNK_SIZE + Consts.CHUNK_SIZE * 0.5f;

    float dx = chunkX - playerPos.x;
    float dz = chunkZ - playerPos.z;
    float distanceSquared = dx * dx + dz * dz;

    float renderDistance = Consts.INIT_CHUNK_RADS * Consts.CHUNK_SIZE;

    return distanceSquared <= renderDistance * renderDistance;
  }

  public void cleanup() {
    if(vboID != 0) {
      glDeleteBuffers(vboID);
      vboID = 0;
    }
    uploaded = false;
  }
}
