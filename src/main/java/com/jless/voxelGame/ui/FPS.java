package com.jless.voxelGame.ui;

import imgui.*;
import imgui.flag.*;

public class FPS {

  public static void drawFPS() {
    ImGui.setNextWindowPos(20, 20);
    ImGui.setNextWindowBgAlpha(0.35f);

    ImGui.begin(
      "##fps",
      ImGuiWindowFlags.NoTitleBar |
      ImGuiWindowFlags.NoMove |
      ImGuiWindowFlags.NoSavedSettings
    );

    ImGui.textColored(
      0.3f, 0.6f, 0.6f, 1f,
      String.format("FPS: %.1f", ImGui.getIO().getFramerate())
    );
    ImGui.end();
  }
}
