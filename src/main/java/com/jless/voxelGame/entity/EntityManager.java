package com.jless.voxelGame.entity;

import java.lang.Math;
import java.util.*;

import org.joml.*;

import com.jless.voxelGame.*;
import com.jless.voxelGame.blocks.*;
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
        trySpawnPig(world, playerPos);
      }
    }
  }

  private void trySpawnPig(World world, Vector3f playerPos) {
    float radius = Consts.ENTITY_SPAWN_RADIUS;

    float angle = (float)(Math.random() * Math.PI * 2);
    float dist = (float)(Math.random() * radius);

    float x = playerPos.x + (float)Math.cos(angle) * dist;
    float z = playerPos.z + (float)Math.sin(angle) * dist;

    int ix = (int)Math.floor(x);
    int iz = (int)Math.floor(z);

    int cx = Math.floorDiv(ix, Consts.CHUNK_SIZE);
    int cz = Math.floorDiv(iz, Consts.CHUNK_SIZE);
    if(world.getChunkIfLoaded(cx, cz) == null) {
      return;
    }

    int y = world.getSurfY(ix, iz);
    if(y < 0) {
      return;
    }

    byte ground = world.getBlockWorld(ix, y, iz);
    if(ground != BlockID.GRASS) {
      return;
    }

    EntityPig pig = new EntityPig(ix + 0.5f, y + 1f, iz + 0.5f);
    entities.add(pig);
  }

  // private void trySpawnPenguin(World world, Vector3f playerPos) {
  //   float radius = Consts.ENTITY_SPAWN_RADIUS;
  //
  //   float angle = (float)(Math.random() * Math.PI * 2);
  //   float dist = (float)(Math.random() * radius);
  //
  //   float x = playerPos.x + (float)Math.cos(angle) * dist;
  //   float z = playerPos.z + (float)Math.sin(angle) * dist;
  //
  //   int ix = (int)Math.floor(x);
  //   int iz = (int)Math.floor(z);
  //
  //   int cx = Math.floorDiv(ix, Consts.CHUNK_SIZE);
  //   int cz = Math.floorDiv(iz, Consts.CHUNK_SIZE);
  //   if(world.getChunkIfLoaded(cx, cz) == null) {
  //     return;
  //   }
  //
  //   int y = world.getSurfY(ix, iz);
  //   if(y < 0) {
  //     return;
  //   }
  //
  //   byte ground = world.getBlockWorld(ix, y, iz);
  //   if(ground != BlockID.GRASS) {
  //     return;
  //   }
  //
  //   EntityPenguin penguin = new EntityPenguin(ix + 0.5f, y + 1f, iz + 0.5f);
  //   entities.add(penguin);
  // }

  public void render(Rendering render) {
    for(Entity e : entities) {
      e.render(render);
    }
  }
}
