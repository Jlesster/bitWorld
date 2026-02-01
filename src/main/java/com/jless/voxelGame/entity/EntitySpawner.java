package com.jless.voxelGame.entity;

import java.lang.Math;
import java.util.*;

import org.joml.*;
import org.joml.Random;

import com.jless.voxelGame.*;
import com.jless.voxelGame.blocks.*;
import com.jless.voxelGame.worldGen.*;

public class EntitySpawner {

  public static class SpawnConfig {
    public float minRadius;
    public float maxRadius;
    public int maxAttempts;
    public int maxEntitiesPerChunk;
    public int maxEntitiesGlobal;
    public float spawnChance;

    public SpawnConfig() {
      this.minRadius = Consts.ENTITY_SPAWN_RADIUS * 0.3f;
      this.maxRadius = Consts.ENTITY_SPAWN_RADIUS;
      this.maxAttempts = 5;
      this.maxEntitiesPerChunk = Consts.MAX_ENITITES / 4;
      this.maxEntitiesGlobal = Consts.MAX_ENITITES;
      this.spawnChance = 0.7f;
    }
  }

  public static class SpawnResult {
    public boolean success;
    public String failureReason;
    public Vector3f position;

    public SpawnResult(boolean success, String reason, Vector3f pos) {
      this.success = success;
      this.failureReason = reason;
      this.position = pos;
    }
  }

  public interface SpawnCondition {
    boolean canSpawn(World world, int x, int y, int z);
    String getFailureReason();
  }

  private final World world;
  private final List<Entity> entities;
  private final SpawnConfig config;
  private final Random random;

  public EntitySpawner(World world, List<Entity> entities, SpawnConfig config) {
    this.world = world;
    this.entities = entities;
    this.config = config;
    this.random = new Random();
  }

  public SpawnResult trySpawnPig(Vector3f playerPos) {
    return trySpawnEntity(playerPos, EntityType.PIG, getPigSpawnConditions());
  }

  public SpawnResult trySpawnEntity(Vector3f centerPos, EntityType type, List<SpawnCondition> conditions) {
    if(entities.size() >= config.maxEntitiesGlobal) return new SpawnResult(false, "Entity limit reached", null);
    if(random.nextFloat() > config.spawnChance) return new SpawnResult(false, "SpawnChantce failed", null);

    for(int attempt = 0; attempt < config.maxAttempts; attempt++) {
      SpawnLocation location = findSpawnLocation(centerPos);
      if(location == null) continue;

      for(SpawnCondition condition : conditions) {
        if(!condition.canSpawn(world, location.x, location.y, location.z)) {
          if(attempt == config.maxAttempts - 1) {
            return new SpawnResult(false, condition.getFailureReason(), null);
          }
          continue;
        }
      }

      Entity entity = createEntity(type, location);
      entities.add(entity);

      Vector3f pos = new Vector3f(
        location.x + 0.5f,
        location.y + 1f,
        location.z + 0.5f
      );
      return new SpawnResult(true, "Spawned successfully", pos);
    }
    return new SpawnResult(false, "No valid spawn locations found", null);
  }

  private SpawnLocation findSpawnLocation(Vector3f centerPos) {
    float radius = config.minRadius + random.nextFloat() * (config.maxRadius - config.minRadius);
    float angle = random.nextFloat() * (float)(Math.PI * 2);

    float x = centerPos.x + (float)Math.cos(angle) * radius;
    float z = centerPos.z + (float)Math.sin(angle) * radius;

    int ix = (int)Math.floor(x);
    int iz = (int)Math.floor(z);
    int cx = Math.floorDiv(ix, Consts.CHUNK_SIZE);
    int cz = Math.floorDiv(iz, Consts.CHUNK_SIZE);

    if(world.getChunkIfLoaded(cx, cz) == null) return null;

    int y = world.getSurfY(ix, iz);
    if(y < 0) return null;

    return new SpawnLocation(ix, y, iz, cx, cz);
  }

  private List<SpawnCondition> getPigSpawnConditions() {
    List<SpawnCondition> conditions = new ArrayList<>();

    conditions.add(new SpawnCondition() {
      @Override
      public boolean canSpawn(World world, int x, int y, int z) {
        byte ground = world.getBlockWorld(x, y, z);
        return ground == BlockID.GRASS;
      }

      @Override
      public String getFailureReason() {
        return "Not spawning on grass";
      }
    });

    conditions.add(new SpawnCondition() {
      @Override
      public boolean canSpawn(World world, int x, int y, int z) {
        int cx = Math.floorDiv(x, Consts.CHUNK_SIZE);
        int cz = Math.floorDiv(x, Consts.CHUNK_SIZE);
        return getEntityCountInChunk(cx, cz) < config.maxEntitiesPerChunk;
      }

      @Override
      public String getFailureReason() {
        return "Chunk entity limit reached";
      }
    });

    conditions.add(new SpawnCondition() {
      @Override
      public boolean canSpawn(World world, int x, int y, int z) {
        byte block1 = world.getBlockWorld(x, y + 1, z);
        byte block2 = world.getBlockWorld(x, y + 2, z);
        return block1 == BlockID.AIR && block2 == BlockID.AIR;
      }

      @Override
      public String getFailureReason() {
        return "Insufficient vert clearance";
      }
    });

    conditions.add(new SpawnCondition() {
      @Override
      public boolean canSpawn(World world, int x, int y, int z) {
        float minDistance = 3.0f;
        for(Entity entity : entities) {
          if(entity instanceof EntityPig) {
            float dx = entity.pos.x - (x + 0.5f);
            float dy = entity.pos.y - (y + 1f);
            float dz = entity.pos.z - (z + 0.5f);
            float distSq = (dx * dx) + (dy * dy) + (dz * dz);
            if(distSq < minDistance * minDistance) return false;
          }
        }
        return true;
      }
      @Override
      public String getFailureReason() {
        return "Too close to pig";
      }
    });

    conditions.add(new SpawnCondition() {
      @Override
      public boolean canSpawn(World world, int x, int y, int z) {
        return true;
      }

      @Override
      public String getFailureReason() {
        return "Insufficient light level";
      }
    });
    return conditions;
  }

  private int getEntityCountInChunk(int cx, int cz) {
    int count = 0;
    for(Entity entity : entities) {
      int ecx = Math.floorDiv((int)Math.floor(entity.pos.x), Consts.CHUNK_SIZE);
      int ecz = Math.floorDiv((int)Math.floor(entity.pos.z), Consts.CHUNK_SIZE);
      if(ecx == cx && ecz == cz) count++;
    }
    return count;
  }

  private Entity createEntity(EntityType type, SpawnLocation loc) {
    switch(type) {
      case PIG:
        return new EntityPig(loc.x + 0.5f, loc.y + 1f, loc.z + 0.5f);

      default:
        throw new IllegalArgumentException("Unknow entity type: " + type);
    }
  }

  private static class SpawnLocation {
    int x, y, z, cx, cz;

    SpawnLocation(int x, int y, int z, int cx, int cz) {
      this.x = x;
      this.y = y;
      this.z = z;
      this.cx = cx;
      this.cz = cz;
    }
  }

  public enum EntityType {
    PIG,
    COW,
    SHEEP,
    CHICKEN,
    ZOMBIE
  }
}
