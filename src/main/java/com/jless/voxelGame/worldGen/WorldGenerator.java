package com.jless.voxelGame.worldGen;

import com.jless.voxelGame.*;
import com.jless.voxelGame.blocks.*;
import com.jless.voxelGame.chunkGen.*;
import com.jless.voxelGame.worldGen.biomeGen.*;
import com.jless.voxelGame.worldGen.oreGen.*;
import com.jless.voxelGame.worldGen.structureGen.caveGen.*;
import com.jless.voxelGame.worldGen.structureGen.treeGen.*;
import com.jless.voxelGame.worldGen.terrainGen.*;

public class WorldGenerator {

  private long seed;
  private BiomeGenerator biomeGen;
  private TerrainGenerator terrainGen;
  private TreePlacer treePlacer;
  private CaveSystem caveSystem;
  private OreDistributor oreDistributor;

  public WorldGenerator(long seed) {
    this.seed = seed;
    this.biomeGen = new BiomeGenerator(seed);
    this.terrainGen = new TerrainGenerator(seed, biomeGen);
    this.treePlacer = new TreePlacer(seed, biomeGen);
    this.caveSystem = new CaveSystem(seed);
    this.oreDistributor = new OreDistributor(seed);
  }

  public void generateChunk(Chunk c, int cx, int cz) {
    int worldX = cx * Consts.CHUNK_SIZE;
    int worldZ = cz * Consts.CHUNK_SIZE;

    for(int x = 0; x < Consts.CHUNK_SIZE; x++) {
      for(int z = 0; z < Consts.CHUNK_SIZE; z++) {
        int wx = worldX + x;
        int wz = worldZ + z;

        generateColumn(c, x, z, wx, wz);
      }
    }
    c.uploaded = false;
    c.dirty = true;
  }

  private void generateColumn(Chunk c, int lx, int lz, int wx, int wz) {
    BiomeType biome = biomeGen.getBiome(wx, wz);
    int height = terrainGen.getHeight(wx, wz);

    int surfDepth = terrainGen.getSufDepth(wx, wz);
    int subsurfDepth = terrainGen.getSubsurfDepth(wx, wz);

    for(int y = 0; y < 256; y++) {
      byte block = BlockID.AIR;

      if(y == 0) {
        block = BlockID.BEDROCK;
      } else if( y < height - surfDepth - subsurfDepth) {
        block = biome.stoneBlock;
      } else if( y < height - surfDepth) {
        block = biome.subsurfBlock;
      } else if(y < height) {
        block = biome.surfBlock;
      } else if(y < 60) {
        block = BlockID.WATER;
      }
      c.setLocal(lx, y, lz, block);
    }
  }

  public void postProcessChunk(World world, int cx, int cz) {
    if(!world.areNeighborsLoaded(cx, cz)) return;
    caveSystem.generateCaves(world, cx, cz);
    oreDistributor.distributeOres(world, cx, cz);
    treePlacer.placeTrees(world, cx, cz);

    Chunk chunk = world.getChunkIfLoaded(cx, cz);
    if(chunk != null) chunk.dirty = true;
  }
}
