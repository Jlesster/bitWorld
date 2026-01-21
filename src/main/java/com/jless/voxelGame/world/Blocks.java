package com.jless.voxelGame.world;

import com.jless.voxelGame.render.TextureAtlas;

public class Blocks {

  public static final boolean[] SOLID = new boolean[256];

  public static final int[] TEX_TOP = new int[256];
  public static final int[] TEX_BOTTOM = new int[256];
  public static final int[] TEX_SIDE = new int[256];
  public static final int[] TEX_FRONT = new int[256];

  static {
    SOLID[BlockID.AIR & 0xFF] = false;

    SOLID[BlockID.GRASS & 0xFF] = true;
    TEX_TOP[BlockID.GRASS & 0xFF] = TextureAtlas.tile(0, 0);
    TEX_BOTTOM[BlockID.GRASS & 0xFF] = TextureAtlas.tile(0, 0);
    TEX_SIDE[BlockID.GRASS & 0xFF] = TextureAtlas.tile(0, 0);

    SOLID[BlockID.DIRT & 0xFF] = true;
    TEX_TOP[BlockID.DIRT & 0xFF] = TextureAtlas.tile(0, 0);
    TEX_BOTTOM[BlockID.DIRT & 0xFF] = TextureAtlas.tile(0, 0);
    TEX_SIDE[BlockID.DIRT & 0xFF] = TextureAtlas.tile(0, 0);

    SOLID[BlockID.STONE & 0xFF] = true;
    TEX_TOP[BlockID.STONE & 0xFF] = TextureAtlas.tile(0, 11);
    TEX_BOTTOM[BlockID.STONE & 0xFF] = TextureAtlas.tile(0, 11);
    TEX_SIDE[BlockID.STONE & 0xFF] = TextureAtlas.tile(0, 11);

    SOLID[BlockID.COBBLE & 0xFF] = true;
    TEX_TOP[BlockID.COBBLE & 0xFF] = TextureAtlas.tile(0, 0);
    TEX_BOTTOM[BlockID.COBBLE & 0xFF] = TextureAtlas.tile(0, 0);
    TEX_SIDE[BlockID.COBBLE & 0xFF] = TextureAtlas.tile(0, 0);

    SOLID[BlockID.OAK_LOG & 0xFF] = true;
    TEX_TOP[BlockID.OAK_LOG & 0xFF] = TextureAtlas.tile(0, 0);
    TEX_BOTTOM[BlockID.OAK_LOG & 0xFF] = TextureAtlas.tile(0, 0);
    TEX_SIDE[BlockID.OAK_LOG & 0xFF] = TextureAtlas.tile(0, 0);

    SOLID[BlockID.OAK_PLANK & 0xFF] = true;
    TEX_TOP[BlockID.OAK_PLANK & 0xFF] = TextureAtlas.tile(0, 0);
    TEX_BOTTOM[BlockID.OAK_PLANK & 0xFF] = TextureAtlas.tile(0, 0);
    TEX_SIDE[BlockID.OAK_PLANK & 0xFF] = TextureAtlas.tile(0, 0);

    SOLID[BlockID.OAK_LEAVES & 0xFF] = true;
    TEX_TOP[BlockID.OAK_LEAVES & 0xFF] = TextureAtlas.tile(0, 0);
    TEX_BOTTOM[BlockID.OAK_LEAVES & 0xFF] = TextureAtlas.tile(0, 0);
    TEX_SIDE[BlockID.OAK_LEAVES & 0xFF] = TextureAtlas.tile(0, 0);

    SOLID[BlockID.SPRUCE_PLANK & 0xFF] = true;
    TEX_TOP[BlockID.SPRUCE_PLANK & 0xFF] = TextureAtlas.tile(0, 0);
    TEX_BOTTOM[BlockID.SPRUCE_PLANK & 0xFF] = TextureAtlas.tile(0, 0);
    TEX_SIDE[BlockID.SPRUCE_PLANK & 0xFF] = TextureAtlas.tile(0, 0);

    SOLID[BlockID.SPRUCE_LOG & 0xFF] = true;
    TEX_TOP[BlockID.SPRUCE_LOG & 0xFF] = TextureAtlas.tile(0, 0);
    TEX_BOTTOM[BlockID.SPRUCE_LOG & 0xFF] = TextureAtlas.tile(0, 0);
    TEX_SIDE[BlockID.SPRUCE_LOG & 0xFF] = TextureAtlas.tile(0, 0);

    SOLID[BlockID.SPRUCE_PLANK & 0xFF] = true;
    TEX_TOP[BlockID.SPRUCE_PLANK & 0xFF] = TextureAtlas.tile(0, 0);
    TEX_BOTTOM[BlockID.SPRUCE_PLANK & 0xFF] = TextureAtlas.tile(0, 0);
    TEX_SIDE[BlockID.SPRUCE_PLANK & 0xFF] = TextureAtlas.tile(0, 0);

    SOLID[BlockID.GLASS & 0xFF] = true;
    TEX_TOP[BlockID.GLASS & 0xFF] = TextureAtlas.tile(0, 0);
    TEX_BOTTOM[BlockID.GLASS & 0xFF] = TextureAtlas.tile(0, 0);
    TEX_SIDE[BlockID.GLASS & 0xFF] = TextureAtlas.tile(0, 0);

    SOLID[BlockID.IRON_ORE & 0xFF] = true;
    TEX_TOP[BlockID.IRON_ORE & 0xFF] = TextureAtlas.tile(0, 0);
    TEX_BOTTOM[BlockID.IRON_ORE & 0xFF] = TextureAtlas.tile(0, 0);
    TEX_SIDE[BlockID.IRON_ORE & 0xFF] = TextureAtlas.tile(0, 0);

    SOLID[BlockID.COAL_ORE & 0xFF] = true;
    TEX_TOP[BlockID.COAL_ORE & 0xFF] = TextureAtlas.tile(0, 0);
    TEX_BOTTOM[BlockID.COAL_ORE & 0xFF] = TextureAtlas.tile(0, 0);
    TEX_SIDE[BlockID.COAL_ORE & 0xFF] = TextureAtlas.tile(0, 0);

    SOLID[BlockID.DIAMOND_ORE & 0xFF] = true;
    TEX_TOP[BlockID.DIAMOND_ORE & 0xFF] = TextureAtlas.tile(0, 0);
    TEX_BOTTOM[BlockID.DIAMOND_ORE & 0xFF] = TextureAtlas.tile(0, 0);
    TEX_SIDE[BlockID.DIAMOND_ORE & 0xFF] = TextureAtlas.tile(0, 0);

    SOLID[BlockID.COPPER_ORE & 0xFF] = true;
    TEX_TOP[BlockID.COPPER_ORE & 0xFF] = TextureAtlas.tile(0, 0);
    TEX_BOTTOM[BlockID.COPPER_ORE & 0xFF] = TextureAtlas.tile(0, 0);
    TEX_SIDE[BlockID.COPPER_ORE & 0xFF] = TextureAtlas.tile(0, 0);

    SOLID[BlockID.SAND & 0xFF] = true;
    TEX_TOP[BlockID.SAND & 0xFF] = TextureAtlas.tile(0, 0);
    TEX_BOTTOM[BlockID.SAND & 0xFF] = TextureAtlas.tile(0, 0);
    TEX_SIDE[BlockID.SAND & 0xFF] = TextureAtlas.tile(0, 0);

    SOLID[BlockID.IRON_BLOCK & 0xFF] = true;
    TEX_TOP[BlockID.IRON_BLOCK & 0xFF] = TextureAtlas.tile(0, 0);
    TEX_BOTTOM[BlockID.IRON_BLOCK & 0xFF] = TextureAtlas.tile(0, 0);
    TEX_SIDE[BlockID.IRON_BLOCK & 0xFF] = TextureAtlas.tile(0, 0);

    SOLID[BlockID.COPPER_BLOCK & 0xFF] = true;
    TEX_TOP[BlockID.COPPER_BLOCK & 0xFF] = TextureAtlas.tile(0, 0);
    TEX_BOTTOM[BlockID.COPPER_BLOCK & 0xFF] = TextureAtlas.tile(0, 0);
    TEX_SIDE[BlockID.COPPER_BLOCK & 0xFF] = TextureAtlas.tile(0, 0);

    SOLID[BlockID.COAL_BLOCK & 0xFF] = true;
    TEX_TOP[BlockID.COAL_BLOCK & 0xFF] = TextureAtlas.tile(0, 0);
    TEX_BOTTOM[BlockID.COAL_BLOCK & 0xFF] = TextureAtlas.tile(0, 0);
    TEX_SIDE[BlockID.COAL_BLOCK & 0xFF] = TextureAtlas.tile(0, 0);

    SOLID[BlockID.DIAMOND_BLOCK & 0xFF] = true;
    TEX_TOP[BlockID.DIAMOND_BLOCK & 0xFF] = TextureAtlas.tile(0, 0);
    TEX_BOTTOM[BlockID.DIAMOND_BLOCK & 0xFF] = TextureAtlas.tile(0, 0);
    TEX_SIDE[BlockID.DIAMOND_BLOCK & 0xFF] = TextureAtlas.tile(0, 0);

    SOLID[BlockID.FURNACE & 0xFF] = true;
    TEX_TOP[BlockID.FURNACE & 0xFF] = TextureAtlas.tile(0, 0);
    TEX_BOTTOM[BlockID.FURNACE & 0xFF] = TextureAtlas.tile(0, 0);
    TEX_SIDE[BlockID.FURNACE & 0xFF] = TextureAtlas.tile(0, 0);

    SOLID[BlockID.BEDROCK & 0xFF] = true;
    TEX_TOP[BlockID.BEDROCK & 0xFF] = TextureAtlas.tile(0, 0);
    TEX_BOTTOM[BlockID.BEDROCK & 0xFF] = TextureAtlas.tile(0, 0);
    TEX_SIDE[BlockID.BEDROCK & 0xFF] = TextureAtlas.tile(0, 0);
  }

  public static boolean isSolid(byte id) {
    return SOLID[id & 0xFF];
  }

  public static int getTile(byte id, Face face) {
    int i = id & 0xFF;
    return switch(face) {
      case UP -> TEX_TOP[i];
      case DOWN -> TEX_BOTTOM[i];
      default -> TEX_SIDE[i];
    };
  }

  private Blocks() {}
}
