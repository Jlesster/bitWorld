package com.jless.voxelGame.entity;

import java.util.*;

import org.joml.*;

import com.jless.voxelGame.*;
import com.jless.voxelGame.entity.EntitySpawner.*;
import com.jless.voxelGame.worldGen.*;

public class EntityManager {

  private final ArrayList<Entity> entities = new ArrayList<>();
  private EntitySpawner spawner;

  private float spawnTimer = 0.0f;

  public EntityManager() {
    System.out.println("Entity manage constructed");
  }

  public void init(World world) {
    SpawnConfig config = new SpawnConfig();
    spawner = new EntitySpawner(world, entities, config);
  }

  public void update(World world, Vector3f playerPos, float dt) {
    for(int i = 0; i < entities.size(); i++) {
      Entity e = entities.get(i);
      e.update(world, dt);

      float dx = e.pos.x;
      float dz = e.pos.z;

      float dist2 = (dx * dx) + (dz * dz);

      float despawn = Consts.ENTITY_RENDER_DISTANCE;
      if(dist2 > despawn * despawn) e.removed = true;
    }
    entities.removeIf(e -> e.removed);

    spawnTimer -= dt;
    if(spawnTimer <= 0 ) {
      spawnTimer = Consts.ENTITY_SPAWN_INTERVAL;
      if(entities.size() < Consts.MAX_ENITITES) {
        SpawnResult result = spawner.trySpawnPig(playerPos);

        if(result.success) {
          System.out.println("pig spawned at: " + result.position);
        } else {
          System.err.println("Spawn failed");
        }
        System.out.println("entity count: " + entities.size());
      }
    }
  }

  public void render(Rendering render) {
    for(Entity e : entities) {
      e.render(render);
    }
  }

  public ArrayList<Entity> getEntities() {
    return entities;
  }

  public SpawnResult manualSpawn(Vector3f pos, EntityType type) {
    if(spawner == null) {
      return new SpawnResult(false, "Spawner not initialized", null);
    }

    List<SpawnCondition> conditions = new ArrayList<>();
    switch(type) {
      case PIG:
        //TODO get pig spawn conds
        break;
      default:
        break;
    }

    return spawner.trySpawnEntity(pos, type, conditions);
  }
}
