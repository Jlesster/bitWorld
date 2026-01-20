package com.jless.voxelGame.world;

import com.jless.voxelGame.Consts;

public class TerrainGen {
  private final Perlin perlin;

  public TerrainGen(long seed) {
    perlin = new Perlin(seed);
  }

  public void generateChunks(Chunk c) {
    int baseX = c.cx * Consts.CHUNK_X;
    int baseZ = c.cz * Consts.CHUNK_Z;

    for(int lx = 0; lx < Consts.CHUNK_X; lx++) {
      for(int lz = 0; lz < Consts.CHUNK_Z; lz++) {
        int wx = baseX + lx;
        int wz = baseZ + lz;

        float n = perlin.fbm(wx* 0.01f, wz * 0.01f, 4, 0.5f, 2.0f);

        int height = Consts.SEA_LEVEL + (int)(n * 20.0f);

        for(int y = 0; y < Consts.CHUNK_Y; y++) {
          byte id;

          if(y > height) id = BlockID.AIR;
          else if(y == height) id = BlockID.GRASS;
          else if(y > height - 4) id = BlockID.DIRT;
          else id = BlockID.STONE;

          c.setLocal(lx, y, lz, id);
        }
      }
    }
  }
}
