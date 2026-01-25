package com.jless.voxelGame.worldGen;

import java.lang.Math;
import java.util.*;

import org.joml.*;

import com.jless.voxelGame.*;
import com.jless.voxelGame.Thread;
import com.jless.voxelGame.blocks.*;
import com.jless.voxelGame.chunkGen.*;

public class World {

  private final Map<Long, Chunk> chunks = new HashMap<>();
  private static final Map<Long, ArrayList<QueuedBlock>> queue = new HashMap();

  public static final Perlin NOISE = new Perlin(Consts.SEED);

  public Chunk getChunk(int cx, int cz) {
    long key = chunkKey(cx, cz);
    Chunk c = chunks.get(key);
    if(c != null) return c;

    c = new Chunk(new Vector3i(cx, 0, cz));
    GenerateTerrain.fillChunk(c, this);
    applyQueuedBlocks(c);
    chunks.put(key, c);
    return c;
  }

  public void generateSpawn() {
    int r = Consts.INIT_CHUNK_RADS;

    for(int cx = -r; cx <= r; cx++) {
      for(int cz = -r; cz <= r; cz++) {
        getChunk(cx, cz);
      }
    }
  }

  public void generateSpawnAsync(Thread threadManager) {
    int r = Consts.INIT_CHUNK_RADS;
    for(int cx = -r; cx <= r; cx++) {
      for(int cz = -r; cz <= r; cz++) {
        final int finalCX = cx;
        final int finalCZ = cz;
        threadManager.generateChunkAsync(cx, cz).thenAccept(chunk -> {
          chunks.put(World.chunkKey(finalCX, finalCZ), chunk);
        });
      }
    }
  }

  public byte getIfLoaded(int x, int y, int z) {
    int cx = ChunkCoords.chunk(x);
    int cz = ChunkCoords.chunk(z);

    Chunk chunk = chunks.get(chunkKey(cx, cz));
    if(chunk == null) {
      return BlockID.AIR;
    }
    int lx = ChunkCoords.local(x);
    int lz = ChunkCoords.local(z);

    return chunk.get(lx, y, lz);
  }

  public byte get(int x, int y, int z) {
    int cx = ChunkCoords.chunk(x);
    int cz = ChunkCoords.chunk(z);

    Chunk chunk = getChunk(cx, cz);

    int lx = ChunkCoords.chunk(x);
    int lz = ChunkCoords.chunk(z);

    return chunk.get(lx, y, lz);
  }

  public void set(int x, int y, int z, byte id) {
    int cx = ChunkCoords.chunk(x);
    int cz = ChunkCoords.chunk(z);

    Chunk chunk = getChunk(cx, cz);

    int lx = ChunkCoords.local(x);
    int lz = ChunkCoords.local(z);

    chunk.set(lx, y, lz, id);
    chunk.markDirty();

    if(lx == 0) {
      markNeighborDirty(cx - 1, cz);
    }
    if(lx == Consts.CHUNK_SIZE - 1) {
      markNeighborDirty(cx + 1, cz);
    }
    if(lz == 0) {
      markNeighborDirty(cx, cz - 1);
    }
    if(lz == Consts.CHUNK_SIZE - 1) {
      markNeighborDirty(cx, cz + 1);
    }
  }

  public static long chunkKey(int cx, int cz) {
    return (((long)cx) << 32) ^ (cz & 0xffffffffL);
  }

  private void markNeighborDirty(int x, int z) {
    Chunk neighbor = chunks.get(chunkKey(x, z));
    if(neighbor != null) {
      neighbor.markDirty();
    }
  }

  public byte getBlockWorld(int wx, int wy, int wz) {
    Chunk c = getChunkIfLoaded(Math.floorDiv(wx, Consts.CHUNK_SIZE), Math.floorDiv(wz, Consts.CHUNK_SIZE));

    if(c == null) return BlockID.AIR;

    int lx = Math.floorMod(wx, Consts.CHUNK_SIZE);
    int lz = Math.floorMod(wz, Consts.CHUNK_SIZE);
    return c.getBlockMap().get(lx, wy, lz);
  }

  public int getSurfY(int wx, int wz) {
    for(int y = Consts.WORLD_HEIGHT - 2; y >= 1; y--) {
      byte b = getBlockWorld(wx, y, wz);
      if(b != BlockID.AIR) return y;
    }
    return -1;
  }

  public Iterable<Chunk> getLoadedChunks() {
    return chunks.values();
  }

  public Chunk getChunkIfLoaded(int cx, int cz) {
    return chunks.get(chunkKey(cx, cz));
  }

  public void queueBlock(int wx, int wy, int wz, byte id) {
    int cx = Math.floorDiv(wx, Consts.CHUNK_SIZE);
    int cz = Math.floorDiv(wz, Consts.CHUNK_SIZE);

    long key = chunkKey(cx, cz);
    queue.computeIfAbsent(key, k -> new ArrayList<>()).add(new QueuedBlock(wx, wy, wz, id));
  }

  public static void applyQueuedBlocks(Chunk c) {
    long key = chunkKey(c.pos.x, c.pos.z);

    ArrayList<QueuedBlock> list = queue.remove(key);
    if(list == null) return;

    BlockMap map = c.getBlockMap();
    for(QueuedBlock qb : list) {
      int lx = Math.floorMod(qb.wx, Consts.CHUNK_SIZE);
      int lz = Math.floorMod(qb.wz, Consts.CHUNK_SIZE);

      if(qb.wy < 0 || qb.wy >= map.sizeY()) continue;
      map.set(lx, qb.wy, lz, qb.id);
    }
    c.markDirty();
  }
}
