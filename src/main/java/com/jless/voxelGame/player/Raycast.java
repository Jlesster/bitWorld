package com.jless.voxelGame.player;

import java.lang.Math;

import org.joml.*;

import com.jless.voxelGame.blocks.*;
import com.jless.voxelGame.worldGen.*;

public class Raycast {

  public static RaycastHit raycast(
    World world,
    Vector3f origin,
    Vector3f direction,
    float maxDist
  ) {
    Vector3f dir = new Vector3f(direction).normalize();

    float x = origin.x;
    float y = origin.y;
    float z = origin.z;

    int vx = (int)Math.floor(x);
    int vy = (int)Math.floor(y);
    int vz = (int)Math.floor(z);

    int stepX = dir.x > 0 ? 1 : -1;
    int stepY = dir.y > 0 ? 1 : -1;
    int stepZ = dir.z > 0 ? 1 : -1;

    float tDeltaX = dir.x == 0 ? Float.MAX_VALUE : Math.abs(1f / dir.x);
    float tDeltaY = dir.y == 0 ? Float.MAX_VALUE : Math.abs(1f / dir.y);
    float tDeltaZ = dir.z == 0 ? Float.MAX_VALUE : Math.abs(1f / dir.z);

    float tMaxX = dir.x > 0 ? ((vx + 1) - x) / dir.x : (x - vx) / -dir.x;
    float tMaxY = dir.y > 0 ? ((vy + 1) - y) / dir.y : (y - vy) / -dir.y;
    float tMaxZ = dir.z > 0 ? ((vz + 1) - z) / dir.z : (z - vz) / -dir.z;

    if(dir.x == 0) tMaxX = Float.MAX_VALUE;
    if(dir.y == 0) tMaxY = Float.MAX_VALUE;
    if(dir.z == 0) tMaxZ = Float.MAX_VALUE;

    Vector3i hitNormal = new Vector3i();
    float dist = 0f;

    while(dist <= maxDist) {
      byte id = world.getIfLoaded(vx, vy, vz);
      if(Blocks.SOLID[id]) {
        return new RaycastHit(new Vector3i(vx, vy, vz), new Vector3i(hitNormal));
      }

      if(tMaxX < tMaxY) {
        if(tMaxX < tMaxZ) {
          vx += stepX;
          dist = tMaxX;
          tMaxX += tDeltaX;
          hitNormal.set(-stepX, 0, 0);
        } else {
          vz += stepZ;
          dist = tMaxZ;
          tMaxZ += tDeltaZ;
          hitNormal.set(0, 0, -stepZ);
        }
      } else {
        if(tMaxY < tMaxZ) {
          vy += stepY;
          dist = tMaxY;
          tMaxY += tDeltaY;
          hitNormal.set(0, -stepY, 0);
        } else {
          vz += stepZ;
          dist = tMaxZ;
          tMaxZ += tDeltaZ;
          hitNormal.set(0, 0, -stepZ);
        }
      }
    }
    return null;
  }
}
