package com.jless.voxelGame.worldGen.structureGen.treeGen;

import com.jless.voxelGame.blocks.*;
import com.jless.voxelGame.worldGen.*;

public class OakTreeGenerator extends TreeGenerator {

  public OakTreeGenerator(long seed) {
    super(seed);
  }

	@Override
	public void generate(World world, int x, int y, int z) {
	  int trunkHeight = 4 + random.nextInt(3);

	  if(!canPlaceTree(world, x, y, z, trunkHeight + 4)) return;

	  for(int dy = 0; dy < trunkHeight; dy++) placeLog(world, x, y + dy, z, BlockID.OAK_LOG);

	  int leafStart = trunkHeight - 2;
	  int leafHeight = 4;

	  for(int dy = 0; dy < leafHeight; dy++) {
	    int currentY = y + leafStart + dy;
	    int radius = getLeafRadius(dy, leafHeight);

	    for(int dx = -radius; dx <= radius; dx++) {
	      for(int dz = -radius; dz <= radius; dz++) {
	        int distSq = dx * dx + dz * dz;
	        if(distSq == radius * radius && random.nextFloat() < 0.3f) continue;

	        placeLeaves(world, x + dx, currentY, z + dz, BlockID.OAK_LEAVES);
	      }
	    }
	  }
	}

  private int getLeafRadius(int layer, int totalLayers) {
    if(layer == 0) return 2;
    if(layer == totalLayers - 1) return 1;
    return 2;
  }
}
