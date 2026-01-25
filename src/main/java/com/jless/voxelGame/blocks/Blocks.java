package com.jless.voxelGame.blocks;

import com.jless.voxelGame.texture.*;

public class Blocks {

  public static final boolean[] SOLID = new boolean[256];

  public static final int[] TEX_TOP = new int[256];
  public static final int[] TEX_BOTTOM = new int[256];
  public static final int[] TEX_SIDE = new int[256];
  public static final int[] TEX_FRONT = new int[256];

  static {
    SOLID[BlockID.AIR & 0xFF] = false;

    SOLID[BlockID.GRASS & 0xFF] = true;
    TEX_TOP[BlockID.GRASS & 0xFF] = TextureAtlas.tile(1, 11);
    TEX_BOTTOM[BlockID.GRASS & 0xFF] = TextureAtlas.tile(3, 11);
    TEX_SIDE[BlockID.GRASS & 0xFF] = TextureAtlas.tile(2, 11);

    SOLID[BlockID.DIRT & 0xFF] = true;
    TEX_TOP[BlockID.DIRT & 0xFF] = TextureAtlas.tile(3, 11);
    TEX_BOTTOM[BlockID.DIRT & 0xFF] = TextureAtlas.tile(3, 11);
    TEX_SIDE[BlockID.DIRT & 0xFF] = TextureAtlas.tile(3, 11);

    SOLID[BlockID.STONE & 0xFF] = true;
    TEX_TOP[BlockID.STONE & 0xFF] = TextureAtlas.tile(0, 11);
    TEX_BOTTOM[BlockID.STONE & 0xFF] = TextureAtlas.tile(0, 11);
    TEX_SIDE[BlockID.STONE & 0xFF] = TextureAtlas.tile(0, 11);

    SOLID[BlockID.COBBLE & 0xFF] = true;
    TEX_TOP[BlockID.COBBLE & 0xFF] = TextureAtlas.tile(0, 10);
    TEX_BOTTOM[BlockID.COBBLE & 0xFF] = TextureAtlas.tile(0, 10);
    TEX_SIDE[BlockID.COBBLE & 0xFF] = TextureAtlas.tile(0, 10);

    SOLID[BlockID.OAK_LOG & 0xFF] = true;
    TEX_TOP[BlockID.OAK_LOG & 0xFF] = TextureAtlas.tile(5, 10);
    TEX_BOTTOM[BlockID.OAK_LOG & 0xFF] = TextureAtlas.tile(5, 10);
    TEX_SIDE[BlockID.OAK_LOG & 0xFF] = TextureAtlas.tile(4, 10);

    SOLID[BlockID.OAK_PLANK & 0xFF] = true;
    TEX_TOP[BlockID.OAK_PLANK & 0xFF] = TextureAtlas.tile(4, 11);
    TEX_BOTTOM[BlockID.OAK_PLANK & 0xFF] = TextureAtlas.tile(4, 11);
    TEX_SIDE[BlockID.OAK_PLANK & 0xFF] = TextureAtlas.tile(4, 11);

    SOLID[BlockID.OAK_LEAVES & 0xFF] = true;
    TEX_TOP[BlockID.OAK_LEAVES & 0xFF] = TextureAtlas.tile(6, 6);
    TEX_BOTTOM[BlockID.OAK_LEAVES & 0xFF] = TextureAtlas.tile(6, 6);
    TEX_SIDE[BlockID.OAK_LEAVES & 0xFF] = TextureAtlas.tile(6, 6);

    SOLID[BlockID.SPRUCE_PLANK & 0xFF] = true;
    TEX_TOP[BlockID.SPRUCE_PLANK & 0xFF] = TextureAtlas.tile(6, 7);
    TEX_BOTTOM[BlockID.SPRUCE_PLANK & 0xFF] = TextureAtlas.tile(6, 7);
    TEX_SIDE[BlockID.SPRUCE_PLANK & 0xFF] = TextureAtlas.tile(6, 7);

    SOLID[BlockID.SPRUCE_LOG & 0xFF] = true;
    TEX_TOP[BlockID.SPRUCE_LOG & 0xFF] = TextureAtlas.tile(5, 7);
    TEX_BOTTOM[BlockID.SPRUCE_LOG & 0xFF] = TextureAtlas.tile(5, 7);
    TEX_SIDE[BlockID.SPRUCE_LOG & 0xFF] = TextureAtlas.tile(4, 7);

    SOLID[BlockID.GLASS & 0xFF] = true;
    TEX_TOP[BlockID.GLASS & 0xFF] = TextureAtlas.tile(3, 10);
    TEX_BOTTOM[BlockID.GLASS & 0xFF] = TextureAtlas.tile(3, 10);
    TEX_SIDE[BlockID.GLASS & 0xFF] = TextureAtlas.tile(3, 10);

    SOLID[BlockID.IRON_ORE & 0xFF] = true;
    TEX_TOP[BlockID.IRON_ORE & 0xFF] = TextureAtlas.tile(1, 0);
    TEX_BOTTOM[BlockID.IRON_ORE & 0xFF] = TextureAtlas.tile(1, 0);
    TEX_SIDE[BlockID.IRON_ORE & 0xFF] = TextureAtlas.tile(1, 0);

    SOLID[BlockID.COAL_ORE & 0xFF] = true;
    TEX_TOP[BlockID.COAL_ORE & 0xFF] = TextureAtlas.tile(10, 11);
    TEX_BOTTOM[BlockID.COAL_ORE & 0xFF] = TextureAtlas.tile(10, 11);
    TEX_SIDE[BlockID.COAL_ORE & 0xFF] = TextureAtlas.tile(10, 11);

    SOLID[BlockID.DIAMOND_ORE & 0xFF] = true;
    TEX_TOP[BlockID.DIAMOND_ORE & 0xFF] = TextureAtlas.tile(0, 10);
    TEX_BOTTOM[BlockID.DIAMOND_ORE & 0xFF] = TextureAtlas.tile(0, 10);
    TEX_SIDE[BlockID.DIAMOND_ORE & 0xFF] = TextureAtlas.tile(0, 10);

    SOLID[BlockID.COPPER_ORE & 0xFF] = true;
    TEX_TOP[BlockID.COPPER_ORE & 0xFF] = TextureAtlas.tile(11, 11);
    TEX_BOTTOM[BlockID.COPPER_ORE & 0xFF] = TextureAtlas.tile(11, 11);
    TEX_SIDE[BlockID.COPPER_ORE & 0xFF] = TextureAtlas.tile(11, 11);

    SOLID[BlockID.SAND & 0xFF] = true;
    TEX_TOP[BlockID.SAND & 0xFF] = TextureAtlas.tile(2, 10);
    TEX_BOTTOM[BlockID.SAND & 0xFF] = TextureAtlas.tile(2, 10);
    TEX_SIDE[BlockID.SAND & 0xFF] = TextureAtlas.tile(2, 10);

    SOLID[BlockID.IRON_BLOCK & 0xFF] = true;
    TEX_TOP[BlockID.IRON_BLOCK & 0xFF] = TextureAtlas.tile(1, 7);
    TEX_BOTTOM[BlockID.IRON_BLOCK & 0xFF] = TextureAtlas.tile(1, 7);
    TEX_SIDE[BlockID.IRON_BLOCK & 0xFF] = TextureAtlas.tile(1, 7);

    SOLID[BlockID.COPPER_BLOCK & 0xFF] = true;
    TEX_TOP[BlockID.COPPER_BLOCK & 0xFF] = TextureAtlas.tile(2, 7);
    TEX_BOTTOM[BlockID.COPPER_BLOCK & 0xFF] = TextureAtlas.tile(2, 7);
    TEX_SIDE[BlockID.COPPER_BLOCK & 0xFF] = TextureAtlas.tile(2, 7);

    SOLID[BlockID.COAL_BLOCK & 0xFF] = true;
    TEX_TOP[BlockID.COAL_BLOCK & 0xFF] = TextureAtlas.tile(9, 9);
    TEX_BOTTOM[BlockID.COAL_BLOCK & 0xFF] = TextureAtlas.tile(9, 9);
    TEX_SIDE[BlockID.COAL_BLOCK & 0xFF] = TextureAtlas.tile(9, 9);

    SOLID[BlockID.DIAMOND_BLOCK & 0xFF] = true;
    TEX_TOP[BlockID.DIAMOND_BLOCK & 0xFF] = TextureAtlas.tile(0, 7);
    TEX_BOTTOM[BlockID.DIAMOND_BLOCK & 0xFF] = TextureAtlas.tile(0, 7);
    TEX_SIDE[BlockID.DIAMOND_BLOCK & 0xFF] = TextureAtlas.tile(0, 7);

    SOLID[BlockID.FURNACE & 0xFF] = true;
    TEX_TOP[BlockID.FURNACE & 0xFF] = TextureAtlas.tile(0, 0);
    TEX_BOTTOM[BlockID.FURNACE & 0xFF] = TextureAtlas.tile(0, 0);
    TEX_SIDE[BlockID.FURNACE & 0xFF] = TextureAtlas.tile(0, 0);

    SOLID[BlockID.BEDROCK & 0xFF] = true;
    TEX_TOP[BlockID.BEDROCK & 0xFF] = TextureAtlas.tile(1, 10);
    TEX_BOTTOM[BlockID.BEDROCK & 0xFF] = TextureAtlas.tile(1, 10);
    TEX_SIDE[BlockID.BEDROCK & 0xFF] = TextureAtlas.tile(1, 10);
  }

  public static boolean isSolid(byte id) {
    return SOLID[id & 0xFF];
  }

  private Blocks() {}
}
