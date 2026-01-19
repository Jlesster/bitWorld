package com.jless.voxelGame.world;

import java.util.HashMap;
import java.util.Map;

import com.jless.voxelGame.Consts;

public class World {

  private final Map<ChunkPos, Chunk> chunks = new HashMap<>();

  public Chunk getOrCreateChunk(int cx, int cz) {
    ChunkPos pos = new ChunkPos(cx, cz);

    Chunk c = chunks.get(pos);
    if(c == null) {
      c = new Chunk(cx, cz);
      chunks.put(pos, c);
    }
    return c;
  }

  public Chunk getChunk(int cx, int cz) {
    return chunks.get(new ChunkPos(cx, cz));
  }

  private int floorDiv(int a, int b) {
    int r = a / b;
    if((a ^ b) < 0 && (r * b != a)) r--;
    return r;
  }

  private int floorMod(int a, int b) {
    int m = a % b;
    if(m < 0) m += b;
    return m;
  }

  public byte getBlock(int wx, int wy, int wz) {
    int cx = floorDiv(wx, Consts.CHUNK_X);
    int cz = floorDiv(wz, Consts.CHUNK_Z);

    Chunk c = getChunk(cx, cz);
    if(c == null) return BlockID.AIR;

    int lx = floorMod(wx, Consts.CHUNK_X);
    int lz = floorMod(wz, Consts.CHUNK_Z);

    return c.getLocal(lx, wy, lz);
  }

  public void setBlock(int  wx, int wy, int wz, byte id) {
    int cx = floorDiv(wx, Consts.CHUNK_X);
    int cz = floorDiv(wz, Consts.CHUNK_Z);

    Chunk c = getOrCreateChunk(cx, cz) ;

    int lx = floorMod(wx, Consts.CHUNK_X);
    int lz = floorMod(wz, Consts.CHUNK_Z);

    c.setLocal(lx, wy, lz, id);
  }
}
