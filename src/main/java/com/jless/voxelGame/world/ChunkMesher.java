package com.jless.voxelGame.world;

import java.util.ArrayList;
import java.util.List;

import com.jless.voxelGame.Consts;
import com.jless.voxelGame.render.Mesh;
import com.jless.voxelGame.render.TextureAtlas;

public class ChunkMesher {

  private final List<Float> verts = new ArrayList<>();
  private final List<Integer> inds = new ArrayList<>();

  public Mesh buildMesh(World world, Chunk chunk, TextureAtlas atlas) {
    verts.clear();
    inds.clear();

    int baseX = chunk.cx * Consts.CHUNK_X;
    int baseZ = chunk.cz * Consts.CHUNK_Z;

    for(int x = 0; x < Consts.CHUNK_X; x++) {
      for(int y = 0; y < Consts.CHUNK_Y; y++) {
        for(int z = 0; z < Consts.CHUNK_Z; z++) {
          byte id = chunk.getLocal(x, y, z);
          if(id == BlockID.AIR) continue;

          int wx = baseX + x;
          int wy = y;
          int wz = baseZ + z;

          for(Face face : Face.values()) {
            int nx = wx + face.dx;
            int ny = wy + face.dy;
            int nz = wz + face.dz;

            byte nid = world.getBlock(nx, ny, nz);
            if(Blocks.isSolid(nid)) continue;

            int tile = Blocks.getTile(id, face);
            addFace(atlas, face, wx, wy, wz, tile);
          }
        }
      }
    }

    if(inds.isEmpty()) return null;

    float[] v = new float[verts.size()];
    for(int i = 0; i < v.length; i++) v[i] = verts.get(i);

    int[] in = new int[inds.size()];
    for(int i = 0; i < in.length; i++) in[i] = inds.get(i);

    System.out.println("Mesher: verts=" + verts.size() + "inds=" + inds.size());
    return new Mesh(v, in);
  }

  private void addFace(TextureAtlas atlas, Face face, float x, float y, float z, int tile) {
    TextureAtlas.UVRect uv = atlas.getUVRect(tile);

    int startIndex = verts.size() / 5;

    switch(face) {
      case NORTH -> quad(
        x, y, z,
        x + 1, y, z,
        x + 1, y + 1, z,
        x, y + 1, z,
        uv, startIndex
      );
      case SOUTH -> quad(
        x + 1, y, z + 1,
        x, y, z + 1,
        x, y + 1, z,
        x + 1, y + 1, z + 1,
        uv, startIndex
      );
      case EAST -> quad(
        x + 1, y, z,
        x + 1, y, z + 1,
        x + 1, y + 1, z + 1,
        x + 1, y + 1, z,
        uv, startIndex
      );
      case WEST -> quad(
        x, y, z + 1,
        x, y, z,
        x, y + 1, z,
        x, y + 1, z + 1,
        uv, startIndex
      );
      case UP -> quad(
        x, y + 1, z,
        x + 1, y + 1, z,
        x + 1, y + 1, z + 1,
        x, y + 1, z + 1,
        uv, startIndex
      );
      case DOWN -> quad(
        x, y, z + 1,
        x + 1, y, z + 1,
        x + 1, y, z,
        x, y, z,
        uv, startIndex
      );
    }
  }

  private void quad(
    float x0, float y0, float z0,
    float x1, float y1, float z1,
    float x2, float y2, float z2,
    float x3, float y3, float z3,
    TextureAtlas.UVRect uv,
    int baseIndex
  ) {
    putVertex(x0, y0, z0, uv.u0, uv.v0);
    putVertex(x1, y1, z1, uv.u1, uv.v0);
    putVertex(x2, y2, z2, uv.u1, uv.v1);
    putVertex(x3, y3, z3, uv.u0, uv.v1);

    inds.add(baseIndex + 0);
    inds.add(baseIndex + 1);
    inds.add(baseIndex + 2);

    inds.add(baseIndex + 2);
    inds.add(baseIndex + 3);
    inds.add(baseIndex + 0);
  }

  private void putVertex(float x, float y, float z, float u, float v) {
    verts.add(x);
    verts.add(y);
    verts.add(z);
    verts.add(u);
    verts.add(v);
  }
}
