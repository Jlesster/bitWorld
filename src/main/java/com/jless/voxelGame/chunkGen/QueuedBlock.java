package com.jless.voxelGame.chunkGen;

public class QueuedBlock {

  public final int wx, wy, wz;
  public final byte id;

  public QueuedBlock(int wx, int wy, int wz, byte id) {
    this.wx = wx;
    this.wy = wy;
    this.wz = wz;
    this.id = id;
  }
}
