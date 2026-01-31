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

  public static final Object GL_ID_LOCK = new Object();
  private boolean greedyMeshingEnabled = Consts.ENABLE_GREEDY_MESHING;

  private FloatBuffer pendingMeshData = null;
  private int pendingVertCount = 0;

  public volatile boolean uploaded = false;
  public boolean dirty = true;
  public volatile int vertCount = 0;
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
    int calculatedVertCount;
    if(greedyMeshingEnabled) {
      calculatedVertCount = data.limit() / 9;
    } else {
      calculatedVertCount = this.vertCount;
    }
    System.out.println("buildMesh: pos=" + pos + " dataLimit=" + data.limit() + " vertCount=" + vertCount);
    return data;
  }

  public void uploadToGPU(FloatBuffer data, int vCount) {
    if(data == null || data.limit() == 0) {
      System.err.println("Err: uploadToGPU called with null value");
      dirty = false;
      return;
    }

    int expectedVerts = data.limit() / 9;
    if(vCount != expectedVerts) {
      System.err.println("WARN: VertCount mismatch");
      vCount = expectedVerts;
    }

    synchronized(GL_ID_LOCK) {
      if(vboID == 0) {
        vboID = glGenBuffers();
      }
      if(vaoID == 0) {
        vaoID = glGenVertexArrays();
      }
      if(vboID == vaoID) {
        System.err.println("CRITICAL ERROR: VBO==VAO");
        vaoID = glGenVertexArrays();
        System.err.println("----REGENERATED VAO----");
      }
    }
    System.out.println("DEBUG: uploadToGPU: " + pos + " vaoID=" + vaoID + ", vboID=" + vboID + ", dataSize=" + data.limit());

    glBindVertexArray(vaoID);
    glBindBuffer(GL_ARRAY_BUFFER, vboID);
    glBufferData(GL_ARRAY_BUFFER, data, GL_STATIC_DRAW);

    int stride = 9 * Float.BYTES;
    glEnableVertexAttribArray(0);
    glEnableVertexAttribArray(1);
    glEnableVertexAttribArray(2);
    glEnableVertexAttribArray(3);
    glVertexAttribPointer(0, 3, GL_FLOAT, false, stride, 0L);
    glVertexAttribPointer(1, 3, GL_FLOAT, false, stride, 3L * Float.BYTES);
    glVertexAttribPointer(2, 2, GL_FLOAT, false, stride, 6L * Float.BYTES);
    glVertexAttribPointer(3, 1, GL_FLOAT, false, stride, 8L * Float.BYTES);

    glBindBuffer(GL_ARRAY_BUFFER, 0);
    glBindVertexArray(0);

    this.vertCount = vCount;
    dirty = false;
    uploaded = true;
    System.out.println("DEBUG uploadToGPU complete: chunk=" + pos + " uploaded=" + uploaded);
  }

  public void uploadToGPU(FloatBuffer data) {
    int vCount = greedyMeshingEnabled ? (data.limit() / 9) : vertCount;
    uploadToGPU(data, vCount);
  }

  private FloatBuffer buildMeshBuffer(World w) {
    if(greedyMeshingEnabled) {
      GreedyMesher mesher = new GreedyMesher();
      return mesher.buildGreedyMesh(this, w);
    } else {
      return buildNativeMesh(w);
    }
  }

  public void buildMeshAsync(World w) {
    FloatBuffer data = buildMesh(w);
    int calculatedVertCount;
    if(greedyMeshingEnabled) {
      calculatedVertCount = data.limit() / 9;
    } else {
      calculatedVertCount = this.vertCount;
    }

    synchronized(this) {
    if(pendingMeshData == null) {
        pendingMeshData = data;
        pendingVertCount = calculatedVertCount;
      } else {
        System.out.println("Skipping mesh update for " + pos);
      }
    }
  }

  private FloatBuffer buildNativeMesh(World w) {
    int initFloats = 2_500_000;
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

  public boolean uploadPendingMesh() {
    FloatBuffer data;
    int vCount;
    synchronized(this) {
      data = pendingMeshData;
      vCount = pendingVertCount;
      pendingMeshData = null;
      pendingVertCount = 0;
    }
    if(data != null && data.limit() > 0) {
      uploadToGPU(data, vCount);
      return true;
    } else {
      System.err.println("uploadPendingMesh has no pending data");
      return false;
    }
  }

  public void ensureUploaded(World w) {
    if(!dirty && uploaded) return;

    FloatBuffer data = buildMesh(w);
    uploadToGPU(data);
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

    float u0 = 0f;
    float v0 = 0f;
    float u1 = 1f;
    float v1 = 1f;
    float layer = TextureAtlas.toLayer(packedTile);
if(x < 3 && y == 64 && z < 3) {
    System.out.println("Block ID: " + id + ", Face: " + face +
                       ", PackedTile: " + packedTile +
                       ", Layer: " + layer +
                       ", tx: " + TextureAtlas.tileX(packedTile) +
                       ", ty: " + TextureAtlas.tileY(packedTile));
}

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
    putV(b, ax, ay, az, nx, ny, nz, au, av, layer);
    putV(b, bx, by, bz, nx, ny, nz, bu, bv, layer);
    putV(b, cx, cy, cz, nx, ny, nz, cu, cv, layer);

    putV(b, ax, ay, az, nx, ny, nz, au, av, layer);
    putV(b, cx, cy, cz, nx, ny, nz, cu, cv, layer);
    putV(b, dx, dy, dz, nx, ny, nz, du, dv, layer);

    return 6;
  }

  public void putV(FloatBuffer b,
  float px, float py, float pz,
  float nx, float ny, float nz,
  float u, float v, float layer) {
    b.put(px).put(py).put(pz);
    b.put(nx).put(ny).put(nz);
    b.put(u).put(v).put(layer);
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

  public boolean hasAllNeighbors(World w) {
    int cx = pos.x;
    int cz = pos.z;

    return w.getChunkIfLoaded(cx + 1, cz) != null &&
           w.getChunkIfLoaded(cx - 1, cz) != null &&
           w.getChunkIfLoaded(cx, cz + 1) != null &&
           w.getChunkIfLoaded(cx, cz - 1) != null;
  }

  public synchronized boolean hasPendingMesh() {
    return pendingMeshData != null && pendingMeshData.limit() > 0;
  }

  public static boolean isChunkVisible(Chunk c, Vector3f playerPos) {
    float chunkX = c.pos.x * Consts.CHUNK_SIZE + Consts.CHUNK_SIZE * 0.5f;
    float chunkZ = c.pos.z * Consts.CHUNK_SIZE + Consts.CHUNK_SIZE * 0.5f;

    float dx = chunkX - playerPos.x;
    float dz = chunkZ - playerPos.z;
    float distanceSquared = dx * dx + dz * dz;

    float renderDistance = Consts.RENDER_DISTANCE * Consts.CHUNK_SIZE;
    if(distanceSquared > renderDistance * renderDistance) return false;

    // float closeDistance = Consts.CHUNK_SIZE * 2;
    // if(distanceSquared < closeDistance * closeDistance) return true;
    //
    // float padding = 2.0f;
    // float minX = c.pos.x * Consts.CHUNK_SIZE - padding;
    // float minZ = c.pos.z * Consts.CHUNK_SIZE - padding;
    // float maxX = minX + Consts.CHUNK_SIZE + padding * 2;
    // float maxZ = minZ + Consts.CHUNK_SIZE + padding * 2;
    // float minY = -padding;
    // float maxY = Consts.WORLD_HEIGHT + padding;
    //
    // return Rendering.frustum.testAab(minX, minY, minZ, maxX, maxY, maxZ);
    return distanceSquared <= renderDistance * renderDistance;
  }

  public void cleanup() {
    if(vboID != 0) {
      glDeleteBuffers(vboID);
      vboID = 0;
    }
    if(vaoID != 0) {
      glDeleteBuffers(vaoID);
      vaoID = 0;
    }
    uploaded = false;
  }
}
