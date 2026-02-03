package com.jless.voxelGame.worldGen;

import java.lang.Math;
import java.util.*;
import java.util.concurrent.*;

import org.joml.*;

import com.jless.voxelGame.*;
import com.jless.voxelGame.blocks.*;
import com.jless.voxelGame.chunkGen.*;
import com.jless.voxelGame.player.*;;

public class World {

  public final ConcurrentHashMap<Long, Chunk> chunks = new ConcurrentHashMap<>();
  private final Set<Long> requestedChunks = ConcurrentHashMap.newKeySet();
  private final Set<Long> postProcessedChunks = ConcurrentHashMap.newKeySet();
  private final Map<Long, Long> recentlyGenerated = new ConcurrentHashMap<>();
  private static final ConcurrentHashMap<Long, ConcurrentLinkedQueue<QueuedBlock>> queue = new ConcurrentHashMap();

  private WorldGenerator worldGenerator;
  private final Set<Long> generatedChunks = ConcurrentHashMap.newKeySet();
  private final ConcurrentLinkedQueue<ChunkCoord> postProcessQueue = new ConcurrentLinkedQueue<>();

  private static final long UNLOAD_GRACE_MS = 5000;

  public static class ChunkCoord {
    final int x, z;
    ChunkCoord(int x, int z) {
      this.x = x;
      this.z = z;
    }

    @Override
    public boolean equals(Object o) {
      if(this == o) return true;
      if(!(o instanceof ChunkCoord)) return false;
      ChunkCoord that = (ChunkCoord) o;
      return x == that.x && z == that.z;
    }

    @Override
    public int hashCode() {
      return Objects.hash(x, z);
    }
  }

  public World() {
    this.worldGenerator = new WorldGenerator(Consts.SEED);
  }

  public synchronized Chunk getChunk(int cx, int cz) {
    return chunks.get(chunkKey(cx, cz));
  }

  public void processPostProcessQueue() {
    int maxProcessedPerFrame = 4;
    int processed = 0;

    Iterator<ChunkCoord> iter = postProcessQueue.iterator();
    while(iter.hasNext() && processed < maxProcessedPerFrame) {
      ChunkCoord coord = iter.next();
      if(hasAllNeighborsGenerated(coord.x, coord.z)) {
        long key = chunkKey(coord.x, coord.z);
        if(postProcessedChunks.contains(key)) {
          iter.remove();
          continue;
        }
        worldGenerator.postProcessChunk(this, coord.x, coord.z);
        postProcessedChunks.add(key);
        Chunk chunk = getChunkIfLoaded(coord.x, coord.z);
        if(chunk != null) chunk.markDirty();

        iter.remove();
        processed++;
      }
    }
  }

  private boolean hasAllNeighborsGenerated(int cx, int cz) {
    for(int dx = -1; dx <= 1; dx++) {
      for(int dz = -1; dz <= 1; dz++) {
        long key = chunkKey(cx + dx, cz + dz);
        if(!generatedChunks.contains(key)) return false;
      }
    }
    return true;
  }

  public void generateChunkTerrain(Chunk c, int cx, int cz) {
    worldGenerator.generateChunk(c, cx, cz);
    applyQueuedBlocks(c);
  }

  public void generateSpawn() {
    int r = Consts.INIT_CHUNK_RADS;

    for(int cx = -r; cx <= r; cx++) {
      for(int cz = -r; cz <= r; cz++) {
        getChunk(cx, cz);
      }
    }

    while(!postProcessQueue.isEmpty()) processPostProcessQueue();
  }

  public void addChunkDirectly(Chunk chunk) {
    long key = chunkKey(chunk.pos.x, chunk.pos.z);
    chunks.put(key, chunk);
    generatedChunks.add(key);

    recentlyGenerated.put(key, System.currentTimeMillis());

    chunk.markDirty();

    markNeighborDirty(chunk.pos.x - 1, chunk.pos.z);
    markNeighborDirty(chunk.pos.x + 1, chunk.pos.z);
    markNeighborDirty(chunk.pos.x, chunk.pos.z - 1);
    markNeighborDirty(chunk.pos.x, chunk.pos.z + 1);

    ChunkCoord coord = new ChunkCoord(chunk.pos.x, chunk.pos.z);
    if(!postProcessedChunks.contains(key) && !postProcessQueue.contains(coord)) {
      postProcessQueue.add(coord);
    }
  }

  public void generateSpawnAsync(ChunkThreadManager threadManager) {
    int r = Consts.INIT_CHUNK_RADS;
    for(int cx = -r; cx <= r; cx++) {
      for(int cz = -r; cz <= r; cz++) {
        threadManager.requestChunkGeneration(cx, cz);
      }
    }
  }

  public boolean isInWorldLimit(int cx, int cz) {
    int dx = cx;
    int dz = cz;
    return dx * dx + dz * dz <= (Consts.WORLD_LIMIT * Consts.WORLD_LIMIT);
  }

  public byte getIfLoaded(int x, int y, int z) {
    if(y < 0 || y >= Consts.WORLD_HEIGHT) {
      return BlockID.AIR;
    }

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

    Chunk chunk = getChunkIfLoaded(cx, cz);
    if(chunk == null) return BlockID.AIR;

    int lx = ChunkCoords.local(x);
    int lz = ChunkCoords.local(z);

    return chunk.get(lx, y, lz);
  }

  public void set(int x, int y, int z, byte id) {
    int cx = ChunkCoords.chunk(x);
    int cz = ChunkCoords.chunk(z);

    Chunk chunk = getChunkIfLoaded(cx, cz);

    if(chunk == null) return;

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
    int cx = Math.floorDiv(wx, Consts.CHUNK_SIZE);
    int cz = Math.floorDiv(wz, Consts.CHUNK_SIZE);
    if(!isInWorldLimit(cx, cz)) return BlockID.AIR;
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

  public static synchronized void queueBlock(int wx, int wy, int wz, byte id) {
    int cx = Math.floorDiv(wx, Consts.CHUNK_SIZE);
    int cz = Math.floorDiv(wz, Consts.CHUNK_SIZE);

    long key = chunkKey(cx, cz);
    queue.computeIfAbsent(key, k -> new ConcurrentLinkedQueue<>()).add(new QueuedBlock(wx, wy, wz, id));
  }

  public static void applyQueuedBlocks(Chunk c) {
    long key = chunkKey(c.pos.x, c.pos.z);

    ConcurrentLinkedQueue<QueuedBlock> list = queue.remove(key);
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

  public void updateStreaming(int playerCX, int playerCZ, ChunkThreadManager threadManager) {

    Vector3f fwd3 = PlayerController.getForwardDir();
    Vector2f forward = new Vector2f(fwd3.x, fwd3.z).normalize();

    int baseRadius = Consts.STREAM_RADS;
    int forwardBonus = 4;
    int backwardPenalty = 3;

    for(int dx = -baseRadius; dx <= baseRadius; dx++) {
      for(int dz = -baseRadius; dz <= baseRadius; dz++) {
        int cx = playerCX + dx;
        int cz = playerCZ + dz;

        if(!isInWorldLimit(cx, cz)) continue;

        float distSq = dx * dx + dz * dz;
        if(distSq > baseRadius * baseRadius) continue;

        Vector2f dirToChunk = new Vector2f(dx, dz);
        if(dirToChunk.lengthSquared() == 0) continue;
        dirToChunk.normalize();

        float alignment = dirToChunk.dot(forward);

        int effectiveRads = baseRadius;

        if(alignment > 0.6f) {
          effectiveRads += forwardBonus;
        } else if(alignment < -0.4f) {
          effectiveRads -= backwardPenalty;
        }

        if(distSq > effectiveRads * effectiveRads) continue;

        tryQueueChunk(cx, cz, threadManager);
      }
    }

    int loadRadius = Consts.STREAM_RADS;
    int unloadRadius = Consts.STREAM_RADS + 3; // <- tweak (2-6 is typical)
    long now = System.currentTimeMillis();

    int unloadSq = unloadRadius * unloadRadius;

    List<Long> toRemove = new ArrayList<>();

    for(Chunk chunk : chunks.values()) {
      int dx = chunk.pos.x - playerCX;
      int dz = chunk.pos.z - playerCZ;
      int distSq = dx * dx + dz * dz;

      long key = chunkKey(chunk.pos.x, chunk.pos.z);

      // 🛡 Skip chunks that were generated recently
      Long genTime = recentlyGenerated.get(key);
      if(genTime != null && now - genTime < UNLOAD_GRACE_MS) {
        continue;
      }

      if(distSq > unloadSq) {
        chunk.cleanup();
        toRemove.add(key);
      }
    }

    for(Long key : toRemove) {
      // Remove from loaded map
      Chunk removed = chunks.remove(key);

      // Clear request flag if it existed
      requestedChunks.remove(key);

      // CRITICAL: allow post-process again if this chunk comes back later
      postProcessedChunks.remove(key);

      // Optional but recommended: if you treat "generatedChunks" as "currently present"
      generatedChunks.remove(key);
    }
    processPostProcessQueue();
  }

  public boolean areNeighborsLoaded(int cx, int cz) {
    return getChunkIfLoaded(cx + 1, cz) != null &&
           getChunkIfLoaded(cx - 1, cz) != null &&
           getChunkIfLoaded(cx, cz + 1) != null &&
           getChunkIfLoaded(cx, cz - 1) != null;
  }

  private void tryQueueChunk(int cx, int cz, ChunkThreadManager threadManager) {
    if(!isInWorldLimit(cx, cz)) return;

    long key = chunkKey(cx, cz);
    if(chunks.containsKey(key) || requestedChunks.contains(key)) return;

    requestedChunks.add(key);
    threadManager.requestChunkGeneration(cx, cz);
  }

  public void markChunkGenerated(int x, int z) {
    requestedChunks.remove(chunkKey(x, z));
  }
}
