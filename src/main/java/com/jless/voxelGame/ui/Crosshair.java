package com.jless.voxelGame.ui;

import imgui.*;

import com.jless.voxelGame.*;

public class Crosshair {

  public static void drawCrosshair() {
    float cx = Consts.W_WIDTH / 2;
    float cy = Consts.W_HEIGHT / 2;
    float size = 10.0f;

    ImDrawList drawList = ImGui.getBackgroundDrawList();

    int color = ImColor.rgba(255, 255, 255, 255);

    drawList.addLine(cx - size, cy, cx + size, cy, color, 2.0f);
    drawList.addLine(cx, cy - size, cx, cy + size, color, 2.0f);
  }
}
