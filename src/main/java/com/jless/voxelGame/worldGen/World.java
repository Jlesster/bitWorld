package com.jless.voxelGame.worldGen;

import java.util.*;

import com.jless.voxelGame.*;

public class World {

  private final Map<Long, Chunk> chunks = new HashMap<>();
  private static final Map<Long, ArrayList<QueuedBlock>> queue = new HashMap();

  public static final Perlin NOISE = new Perlin(Consts.SEED);
}
