package com.jless.voxelGame.entity;

import java.util.*;

import org.joml.*;

import com.jless.voxelGame.*;
import com.jless.voxelGame.worldGen.*;

public class EntityManager {

  private final ArrayList<Entity> entities = new ArrayList<>();

  private float spawnTimer = 0.0f;

  public EntityManager() {
    System.out.println("Entity manage constructed");
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
        //try spawn entities here
      }
    }
  }

}
