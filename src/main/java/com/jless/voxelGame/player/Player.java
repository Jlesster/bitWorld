package com.jless.voxelGame.player;
import org.joml.*;

import com.jless.voxelGame.blocks.*;
import com.jless.voxelGame.worldGen.*;

public class Player {

  private RaycastHit currHit = null;
  public static boolean breakReq = false;
  public static boolean placeReq = false;
  public byte block = BlockID.GRASS;
  private World world;

  public void blockManip() {
    if(breakReq) {
      breakReq = false;
      System.out.println("Break req fired");

      RaycastHit hit = Raycast.raycast(world, PlayerController.getEyePos(), PlayerController.getForwardDir(), 60.0f);
      if(hit != null) {
        System.out.println("Hit block at: " + hit.block.x + "," + hit.block.y + "," + hit.block.z);
        world.set(hit.block.x, hit.block.y, hit.block.z, BlockID.AIR);
        byte after = world.getIfLoaded(hit.block.x, hit.block.y, hit.block.z);
      } else {
        System.out.println("No hit detected");
      }
    }

    if(placeReq) {
      placeReq = false;
      System.out.println("PlaceReqFired");
      byte placeID = block;

      RaycastHit hit = Raycast.raycast(world, PlayerController.getEyePos(), PlayerController.getForwardDir(), 6.0f);

      if(hit != null) {
        Vector3i p = new Vector3i(hit.block).add(hit.normal);
        if(!PlayerController.wouldCollideWithBlock(p.x, p.y, p.z)) {
          world.set(
            p.x, p.y, p.z, placeID
          );
        }
      }
    }
  }

  public void setSelectedBlock(byte id) {
    this.block = id;
    System.out.println("Selected block: " + block);
  }

  public byte getSelectedBlock() {
    return block;
  }

  public Player(World w) {
    this.world = w;
  }
}
