package com.jless.voxelGame.worldGen.structureGen.treeGen;

import com.jless.voxelGame.blocks.*;
import com.jless.voxelGame.worldGen.*;

public class SpruceTreeGenerator extends TreeGenerator {

  public SpruceTreeGenerator(long seed) {
    super(seed);
  }

	@Override
	public void generate(World world, int x, int y, int z) {
	  int trunkHeight = 6 + random.nextInt(4);

	  if(!canPlaceTree(world, x, y, z, trunkHeight + 3)) return;

	  for(int dy = 0; dy < trunkHeight; dy++) placeLog(world, x, y + dy, z, BlockID.OAK_LOG);

	  int leafStart = 2;

	  for(int dy = leafStart; dy < trunkHeight; dy++) {
	    int currentY = y + dy;
      int layerFromTop = trunkHeight - dy;
      int radius = Math.min(2, (layerFromTop + 1) / 2);

	    for(int dx = -radius; dx <= radius; dx++) {
	      for(int dz = -radius; dz <= radius; dz++) {
	        int dist = Math.max(Math.abs(dx), Math.abs(dz));

          if(dist <= radius) {
            if(dx == 0 && dz == 0) continue;

            placeLeaves(world, x + dx, currentY, z + dz, BlockID.OAK_LEAVES);
          }
	      }
	    }
	  }
	  placeLeaves(world, x, y + trunkHeight, z, BlockID.OAK_LEAVES);
	}
}
