package com.jless.voxelGame.ui;

import imgui.*;
import imgui.gl3.*;
import imgui.glfw.*;

public class UI {

  private ImGuiImplGl3 imGuiGL;
  private ImGuiImplGlfw imGuiGLFW;

  public void initGUI(long window) {
    ImGui.createContext();

    imGuiGLFW = new ImGuiImplGlfw();
    imGuiGLFW.init(window, true);

    imGuiGL = new ImGuiImplGl3();
    imGuiGL.init();
  }

  public void renderGUI() {
    imGuiGLFW.newFrame();
    ImGui.newFrame();

    FPS.drawFPS();
    Crosshair.drawCrosshair();

    ImGui.render();
    imGuiGL.renderDrawData(ImGui.getDrawData());
  }
}
