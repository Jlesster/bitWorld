package com.jless.voxelGame.worldGen.structureGen.caveGen;

import java.util.*;

import com.jless.voxelGame.*;
import com.jless.voxelGame.blocks.*;
import com.jless.voxelGame.worldGen.*;
import com.jless.voxelGame.worldGen.noiseGen.*;

public class CavernGenerator {

  private MultiOctaveNoise cavernNoise;
  private MultiOctaveNoise heightNoise;
  private Random random;

  private static final float CAVERN_SCALE = 80.0f;
  private static final float CAVERN_THRESH = 0.65f;

  public CavernGenerator(long seed) {
    this.cavernNoise = new MultiOctaveNoise(seed + 1000, 3);
    this.heightNoise = new MultiOctaveNoise(seed + 2000, 2);
    this.random = new Random(seed);
  }

  public void generateCaverns(World world, int cx, int cz) {
    int worldX = cx * Consts.CHUNK_SIZE;
    int worldZ = cz * Consts.CHUNK_SIZE;

    long chunkSeed = (long)cx * 341873128712L + (long)cz * 132897987541L;
    random.setSeed(chunkSeed);

    if(random.nextDouble() > 0.05) return;

    int cavernY = 20 + random.nextInt(40);
    int cavernHeight = 15 + random.nextInt(15);

    for(int x = 0; x < Consts.CHUNK_SIZE; x++) {
      for(int z = 0; z < Consts.CHUNK_SIZE; z++) {
        int wx = worldX + x;
        int wz = worldZ + z;

        double cavernStrength = cavernNoise.getNoise(wx, wz, CAVERN_SCALE, 0.5);

        if(cavernStrength > CAVERN_THRESH) {
          double heightVar = heightNoise.getNoise(wx, wz, 30.0f, 0.5);
          int localHeight = cavernHeight + (int)(heightVar * 5);

          int minY = cavernY - localHeight / 2;
          int maxY = cavernY + localHeight / 2;

          for(int y = minY; y < maxY; y++) {
            if(y >= 5 && y < 250) {
              byte currBlock = world.getIfLoaded(wx, y, wz);
              if(Blocks.SOLID[currBlock]) world.set(wx, y, wz, BlockID.AIR);
            }
          }
        }
      }
    }
  }
}
