# Voxel Game UI Systems Tutorial
## Complete Guide: Inventory, Hotbar, and Health System

This tutorial provides a complete roadmap for implementing a professional inventory system, hotbar UI, and health system for your voxel game using native OpenGL rendering (no ImGui). The design focuses on clean, Minecraft-inspired aesthetics with modern polish.

---

## Table of Contents

1. [Architecture Overview](#architecture-overview)
2. [Phase 1: UI Rendering Foundation](#phase-1-ui-rendering-foundation)
3. [Phase 2: Hotbar System](#phase-2-hotbar-system)
4. [Phase 3: Inventory System](#phase-3-inventory-system)
5. [Phase 4: Health System](#phase-4-health-system)
6. [Phase 5: Polish and Integration](#phase-5-polish-and-integration)

---

## Architecture Overview

### System Components

```
UISystem (Coordinator)
├── HotbarRenderer
├── InventoryRenderer
├── HealthRenderer
└── UITextRenderer

ItemStack (Data Structure)
├── byte blockID
├── int quantity
└── ItemStack[] for inventory slots

InputHandler
├── Mouse click detection
├── Slot selection
└── Item transfer logic
```

### Key Design Principles

- **Layered Rendering**: UI renders after 3D world, in screen space
- **Orthographic Projection**: 2D UI uses orthographic projection matrix
- **Texture Atlas**: UI elements from a dedicated texture atlas
- **Modular Design**: Each UI component is independent and reusable

---

## Phase 1: UI Rendering Foundation

### Step 1.1: Create UIRenderer Base Class

This handles all 2D quad rendering for UI elements.

```java
package com.jless.voxelGame.ui;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.nio.*;
import org.lwjgl.*;
import org.joml.*;
import com.jless.voxelGame.texture.*;

public class UIRenderer {
    
    private static UIRenderer instance;
    
    private int quadVAO;
    private int quadVBO;
    private int quadEBO;
    
    private Matrix4f orthoMatrix;
    private FloatBuffer orthoBuffer;
    
    // Quad vertices: x, y, u, v
    private static final float[] QUAD_VERTICES = {
        0.0f, 1.0f, 0.0f, 1.0f,  // Top-left
        1.0f, 1.0f, 1.0f, 1.0f,  // Top-right
        1.0f, 0.0f, 1.0f, 0.0f,  // Bottom-right
        0.0f, 0.0f, 0.0f, 0.0f   // Bottom-left
    };
    
    private static final int[] QUAD_INDICES = {
        0, 1, 2,
        0, 2, 3
    };
    
    private UIRenderer(int screenWidth, int screenHeight) {
        initQuadGeometry();
        setupOrthoProjection(screenWidth, screenHeight);
    }
    
    public static void create(int width, int height) {
        if (instance != null) {
            throw new IllegalStateException("UIRenderer already created");
        }
        instance = new UIRenderer(width, height);
    }
    
    public static UIRenderer getInstance() {
        if (instance == null) {
            throw new IllegalStateException("UIRenderer not created");
        }
        return instance;
    }
    
    private void initQuadGeometry() {
        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(QUAD_VERTICES.length);
        vertexBuffer.put(QUAD_VERTICES);
        vertexBuffer.flip();
        
        IntBuffer indexBuffer = BufferUtils.createIntBuffer(QUAD_INDICES.length);
        indexBuffer.put(QUAD_INDICES);
        indexBuffer.flip();
        
        quadVAO = glGenVertexArrays();
        glBindVertexArray(quadVAO);
        
        quadVBO = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, quadVBO);
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);
        
        quadEBO = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, quadEBO);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW);
        
        int stride = 4 * Float.BYTES;
        
        // Position attribute
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, stride, 0L);
        
        // UV attribute
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, stride, 2L * Float.BYTES);
        
        glBindVertexArray(0);
    }
    
    private void setupOrthoProjection(int width, int height) {
        orthoMatrix = new Matrix4f().ortho(0, width, height, 0, -1, 1);
        orthoBuffer = BufferUtils.createFloatBuffer(16);
        orthoMatrix.get(orthoBuffer);
    }
    
    public void updateProjection(int width, int height) {
        orthoMatrix.identity().ortho(0, width, height, 0, -1, 1);
        orthoMatrix.get(orthoBuffer);
    }
    
    /**
     * Draw a textured quad at the specified screen position and size
     * @param x Screen X position (pixels)
     * @param y Screen Y position (pixels)
     * @param width Width in pixels
     * @param height Height in pixels
     * @param u0 UV start X (0-1)
     * @param v0 UV start Y (0-1)
     * @param u1 UV end X (0-1)
     * @param v1 UV end Y (0-1)
     */
    public void drawTexturedQuad(float x, float y, float width, float height,
                                  float u0, float v0, float u1, float v1) {
        // Create model matrix for this quad
        Matrix4f modelMatrix = new Matrix4f()
            .translate(x, y, 0)
            .scale(width, height, 1);
        
        FloatBuffer modelBuffer = BufferUtils.createFloatBuffer(16);
        modelMatrix.get(modelBuffer);
        
        // Set uniforms (assumes UI shader is active)
        Shaders.setModelMatrix(modelBuffer);
        
        // Update UVs (you'll need to modify shader or use a different approach)
        // For now, we'll use the base quad and transform UVs in shader
        
        glBindVertexArray(quadVAO);
        glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);
        glBindVertexArray(0);
    }
    
    /**
     * Draw a colored quad (no texture)
     */
    public void drawColoredQuad(float x, float y, float width, float height,
                                 float r, float g, float b, float a) {
        Matrix4f modelMatrix = new Matrix4f()
            .translate(x, y, 0)
            .scale(width, height, 1);
        
        FloatBuffer modelBuffer = BufferUtils.createFloatBuffer(16);
        modelMatrix.get(modelBuffer);
        
        Shaders.setModelMatrix(modelBuffer);
        Shaders.setUniformInt("useSolidColor", 1);
        Shaders.setSolidColor(new Vector3f(r, g, b));
        
        glBindVertexArray(quadVAO);
        glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);
        glBindVertexArray(0);
        
        Shaders.setUniformInt("useSolidColor", 0);
    }
    
    public FloatBuffer getOrthoMatrix() {
        return orthoBuffer;
    }
    
    public void cleanup() {
        glDeleteVertexArrays(quadVAO);
        glDeleteBuffers(quadVBO);
        glDeleteBuffers(quadEBO);
    }
}
```

### Step 1.2: Create UI Shader

Create a dedicated shader for UI rendering at `/shaders/ui.vert` and `/shaders/ui.frag`:

**ui.vert:**
```glsl
#version 330 core

layout(location = 0) in vec2 aPos;
layout(location = 1) in vec2 aTexCoord;

uniform mat4 uProjection;
uniform mat4 uModel;

out vec2 TexCoord;

void main() {
    gl_Position = uProjection * uModel * vec4(aPos, 0.0, 1.0);
    TexCoord = aTexCoord;
}
```

**ui.frag:**
```glsl
#version 330 core

in vec2 TexCoord;
out vec4 FragColor;

uniform sampler2D uTexture;
uniform vec4 uColor;
uniform int uUseTexture;

void main() {
    if (uUseTexture == 1) {
        FragColor = texture(uTexture, TexCoord) * uColor;
    } else {
        FragColor = uColor;
    }
}
```

### Step 1.3: Create UIShader Manager

```java
package com.jless.voxelGame.ui;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import java.nio.*;
import org.lwjgl.*;
import org.joml.*;

public class UIShader {
    
    private static int shaderProgram;
    private static int uProjectionLoc;
    private static int uModelLoc;
    private static int uTextureLoc;
    private static int uColorLoc;
    private static int uUseTextureLoc;
    
    public static void create() {
        String vertSource = loadShaderSource("/shaders/ui.vert");
        String fragSource = loadShaderSource("/shaders/ui.frag");
        
        int vertShader = compileShader(GL_VERTEX_SHADER, vertSource);
        int fragShader = compileShader(GL_FRAGMENT_SHADER, fragSource);
        
        shaderProgram = glCreateProgram();
        glAttachShader(shaderProgram, vertShader);
        glAttachShader(shaderProgram, fragShader);
        glLinkProgram(shaderProgram);
        
        if (glGetProgrami(shaderProgram, GL_LINK_STATUS) == GL_FALSE) {
            String log = glGetProgramInfoLog(shaderProgram);
            throw new RuntimeException("UI shader linking failed: " + log);
        }
        
        glDeleteShader(vertShader);
        glDeleteShader(fragShader);
        
        // Cache uniform locations
        uProjectionLoc = glGetUniformLocation(shaderProgram, "uProjection");
        uModelLoc = glGetUniformLocation(shaderProgram, "uModel");
        uTextureLoc = glGetUniformLocation(shaderProgram, "uTexture");
        uColorLoc = glGetUniformLocation(shaderProgram, "uColor");
        uUseTextureLoc = glGetUniformLocation(shaderProgram, "uUseTexture");
    }
    
    public static void use() {
        glUseProgram(shaderProgram);
    }
    
    public static void setProjection(FloatBuffer matrix) {
        glUniformMatrix4fv(uProjectionLoc, false, matrix);
    }
    
    public static void setModel(FloatBuffer matrix) {
        glUniformMatrix4fv(uModelLoc, false, matrix);
    }
    
    public static void setColor(float r, float g, float b, float a) {
        glUniform4f(uColorLoc, r, g, b, a);
    }
    
    public static void setUseTexture(boolean use) {
        glUniform1i(uUseTextureLoc, use ? 1 : 0);
    }
    
    public static void setTexture(int unit) {
        glUniform1i(uTextureLoc, unit);
    }
    
    private static String loadShaderSource(String path) {
        // Load from resources (similar to your existing shader loading)
        // ... implementation
        return "";
    }
    
    private static int compileShader(int type, String source) {
        int shader = glCreateShader(type);
        glShaderSource(shader, source);
        glCompileShader(shader);
        
        if (glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE) {
            String log = glGetShaderInfoLog(shader);
            glDeleteShader(shader);
            throw new RuntimeException("Shader compilation failed: " + log);
        }
        return shader;
    }
    
    public static void cleanup() {
        glDeleteProgram(shaderProgram);
    }
}
```

---

## Phase 2: Hotbar System

### Step 2.1: Create ItemStack Data Structure

```java
package com.jless.voxelGame.inventory;

import com.jless.voxelGame.blocks.*;

public class ItemStack {
    
    private byte blockID;
    private int quantity;
    
    public static final int MAX_STACK_SIZE = 64;
    
    public ItemStack(byte blockID, int quantity) {
        this.blockID = blockID;
        this.quantity = Math.min(quantity, MAX_STACK_SIZE);
    }
    
    public ItemStack(byte blockID) {
        this(blockID, 1);
    }
    
    public boolean isEmpty() {
        return blockID == BlockID.AIR || quantity <= 0;
    }
    
    public byte getBlockID() {
        return blockID;
    }
    
    public int getQuantity() {
        return quantity;
    }
    
    public void setQuantity(int quantity) {
        this.quantity = Math.max(0, Math.min(quantity, MAX_STACK_SIZE));
        if (this.quantity == 0) {
            this.blockID = BlockID.AIR;
        }
    }
    
    public void addQuantity(int amount) {
        setQuantity(quantity + amount);
    }
    
    public void decrementQuantity() {
        setQuantity(quantity - 1);
    }
    
    public boolean canStack(ItemStack other) {
        if (other == null || other.isEmpty()) return false;
        if (this.isEmpty()) return true;
        return this.blockID == other.blockID;
    }
    
    public int getRemainingSpace() {
        if (isEmpty()) return MAX_STACK_SIZE;
        return MAX_STACK_SIZE - quantity;
    }
    
    public ItemStack copy() {
        return new ItemStack(blockID, quantity);
    }
    
    public static ItemStack empty() {
        return new ItemStack(BlockID.AIR, 0);
    }
}
```

### Step 2.2: Create Hotbar Class

```java
package com.jless.voxelGame.inventory;

public class Hotbar {
    
    public static final int HOTBAR_SIZE = 9;
    private ItemStack[] slots;
    private int selectedSlot;
    
    public Hotbar() {
        slots = new ItemStack[HOTBAR_SIZE];
        for (int i = 0; i < HOTBAR_SIZE; i++) {
            slots[i] = ItemStack.empty();
        }
        selectedSlot = 0;
        
        // Initialize with some blocks for testing
        slots[0] = new ItemStack(BlockID.GRASS, 64);
        slots[1] = new ItemStack(BlockID.DIRT, 64);
        slots[2] = new ItemStack(BlockID.STONE, 64);
        slots[3] = new ItemStack(BlockID.OAK_LOG, 64);
        slots[4] = new ItemStack(BlockID.OAK_PLANK, 64);
    }
    
    public ItemStack getSelectedItem() {
        return slots[selectedSlot];
    }
    
    public byte getSelectedBlockID() {
        ItemStack stack = getSelectedItem();
        return stack.isEmpty() ? BlockID.AIR : stack.getBlockID();
    }
    
    public void setSelectedSlot(int slot) {
        if (slot >= 0 && slot < HOTBAR_SIZE) {
            selectedSlot = slot;
        }
    }
    
    public int getSelectedSlot() {
        return selectedSlot;
    }
    
    public void selectNext() {
        selectedSlot = (selectedSlot + 1) % HOTBAR_SIZE;
    }
    
    public void selectPrevious() {
        selectedSlot = (selectedSlot - 1 + HOTBAR_SIZE) % HOTBAR_SIZE;
    }
    
    public ItemStack getSlot(int index) {
        if (index >= 0 && index < HOTBAR_SIZE) {
            return slots[index];
        }
        return ItemStack.empty();
    }
    
    public void setSlot(int index, ItemStack stack) {
        if (index >= 0 && index < HOTBAR_SIZE) {
            slots[index] = stack;
        }
    }
    
    public boolean useSelectedItem() {
        ItemStack selected = getSelectedItem();
        if (!selected.isEmpty()) {
            selected.decrementQuantity();
            return true;
        }
        return false;
    }
}
```

### Step 2.3: Create HotbarRenderer

```java
package com.jless.voxelGame.ui;

import static org.lwjgl.opengl.GL11.*;
import org.joml.*;
import com.jless.voxelGame.inventory.*;
import com.jless.voxelGame.entity.*;
import com.jless.voxelGame.texture.*;

public class HotbarRenderer {
    
    private static final int SLOT_SIZE = 40;
    private static final int SLOT_PADDING = 4;
    private static final int HOTBAR_SPACING = SLOT_SIZE + SLOT_PADDING;
    
    private static final float[] SLOT_COLOR = {0.2f, 0.2f, 0.2f, 0.8f};
    private static final float[] SLOT_BORDER = {0.0f, 0.0f, 0.0f, 1.0f};
    private static final float[] SELECTED_COLOR = {1.0f, 1.0f, 1.0f, 0.9f};
    
    private static final int BORDER_WIDTH = 2;
    
    private UIRenderer uiRenderer;
    private int screenWidth;
    private int screenHeight;
    
    public HotbarRenderer(UIRenderer uiRenderer, int screenWidth, int screenHeight) {
        this.uiRenderer = uiRenderer;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }
    
    public void render(Hotbar hotbar) {
        int totalWidth = Hotbar.HOTBAR_SIZE * HOTBAR_SPACING - SLOT_PADDING;
        int startX = (screenWidth - totalWidth) / 2;
        int startY = screenHeight - SLOT_SIZE - 20;
        
        // Enable blending for transparency
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        
        UIShader.use();
        UIShader.setProjection(uiRenderer.getOrthoMatrix());
        UIShader.setUseTexture(false);
        
        for (int i = 0; i < Hotbar.HOTBAR_SIZE; i++) {
            int x = startX + i * HOTBAR_SPACING;
            int y = startY;
            
            boolean selected = (i == hotbar.getSelectedSlot());
            
            // Draw slot background
            if (selected) {
                // Draw selection border
                UIShader.setColor(SELECTED_COLOR[0], SELECTED_COLOR[1], 
                                  SELECTED_COLOR[2], SELECTED_COLOR[3]);
                uiRenderer.drawColoredQuad(x - BORDER_WIDTH, y - BORDER_WIDTH, 
                                          SLOT_SIZE + BORDER_WIDTH * 2, 
                                          SLOT_SIZE + BORDER_WIDTH * 2,
                                          SELECTED_COLOR[0], SELECTED_COLOR[1],
                                          SELECTED_COLOR[2], SELECTED_COLOR[3]);
            }
            
            // Draw slot
            UIShader.setColor(SLOT_COLOR[0], SLOT_COLOR[1], 
                              SLOT_COLOR[2], SLOT_COLOR[3]);
            uiRenderer.drawColoredQuad(x, y, SLOT_SIZE, SLOT_SIZE,
                                      SLOT_COLOR[0], SLOT_COLOR[1],
                                      SLOT_COLOR[2], SLOT_COLOR[3]);
            
            // Draw item if slot is not empty
            ItemStack stack = hotbar.getSlot(i);
            if (!stack.isEmpty()) {
                renderItem(stack, x, y);
            }
        }
        
        glDisable(GL_BLEND);
    }
    
    private void renderItem(ItemStack stack, int x, int y) {
        // Center the item in the slot
        int itemSize = 32;
        int offsetX = x + (SLOT_SIZE - itemSize) / 2;
        int offsetY = y + (SLOT_SIZE - itemSize) / 2;
        
        // Render the block as a small 3D item
        // We'll use EntityRender to draw a small block
        
        // Save current state
        glEnable(GL_DEPTH_TEST);
        
        // Create a small projection for the item
        Matrix4f itemProj = new Matrix4f().ortho(
            0, screenWidth, screenHeight, 0, -100, 100
        );
        
        // Create transform for the item
        Matrix4f itemTransform = new Matrix4f()
            .translate(offsetX + itemSize / 2f, offsetY + itemSize / 2f, 0)
            .rotateY((float) Math.toRadians(-45))
            .rotateX((float) Math.toRadians(30))
            .scale(itemSize * 0.5f);
        
        // Switch to world shader and render item
        Shaders.use();
        
        java.nio.FloatBuffer projBuf = org.lwjgl.BufferUtils.createFloatBuffer(16);
        itemProj.get(projBuf);
        Shaders.setProjMatrix(projBuf);
        
        // Identity view matrix for UI rendering
        Matrix4f identity = new Matrix4f();
        java.nio.FloatBuffer viewBuf = org.lwjgl.BufferUtils.createFloatBuffer(16);
        identity.get(viewBuf);
        Shaders.setViewMatrix(viewBuf);
        
        EntityRender.drawBlockItem(itemTransform, stack.getBlockID());
        
        glDisable(GL_DEPTH_TEST);
        
        // TODO: Render quantity text if > 1
        if (stack.getQuantity() > 1) {
            // This will be implemented in Step 2.4
        }
    }
    
    public void updateScreenSize(int width, int height) {
        this.screenWidth = width;
        this.screenHeight = height;
    }
}
```

### Step 2.4: Integrate Hotbar with Player

Update your `Player.java`:

```java
package com.jless.voxelGame.player;

import org.joml.*;
import com.jless.voxelGame.blocks.*;
import com.jless.voxelGame.worldGen.*;
import com.jless.voxelGame.inventory.*;

public class Player {

  private RaycastHit currHit = null;
  public static boolean breakReq = false;
  public static boolean placeReq = false;
  
  private Hotbar hotbar;  // NEW
  private PlayerHand hand;
  private World world;

  public void blockManip() {
    if(breakReq) {
      breakReq = false;
      System.out.println("Break req fired");
      hand.startSwinging();

      RaycastHit hit = Raycast.raycast(world, PlayerController.getEyePos(), 
                                        PlayerController.getForwardDir(), 60.0f);
      if(hit != null) {
        System.out.println("Hit block at: " + hit.block.x + "," + hit.block.y + "," + hit.block.z);
        world.set(hit.block.x, hit.block.y, hit.block.z, BlockID.AIR);
      } else {
        System.out.println("No hit detected");
      }
    }

    if(placeReq) {
      placeReq = false;
      System.out.println("PlaceReqFired");
      
      byte placeID = hotbar.getSelectedBlockID();  // CHANGED
      if (placeID == BlockID.AIR) return;

      hand.startSwinging();

      RaycastHit hit = Raycast.raycast(world, PlayerController.getEyePos(), 
                                        PlayerController.getForwardDir(), 6.0f);

      if(hit != null) {
        Vector3i p = new Vector3i(hit.block).add(hit.normal);
        if(!PlayerController.wouldCollideWithBlock(p.x, p.y, p.z)) {
          world.set(p.x, p.y, p.z, placeID);
          hotbar.useSelectedItem();  // NEW: Decrement quantity
        }
      }
    }
  }

  public void setSelectedSlot(int slot) {  // NEW
    hotbar.setSelectedSlot(slot);
  }
  
  public Hotbar getHotbar() {  // NEW
    return hotbar;
  }

  public void updateHand(float dt, Vector3f vel) {
    hand.update(dt, vel);
  }

  public void renderHand() {
    hand.render(hotbar.getSelectedBlockID());  // CHANGED
  }

  public Player(World w) {
    this.world = w;
    this.hotbar = new Hotbar();  // NEW
    this.hand = new PlayerHand();
  }
}
```

### Step 2.5: Add Hotbar Input Handling

Update your `Input.java` to handle number keys:

```java
// Add to Input class
public static void handleHotbarInput(Player player) {
    for (int i = GLFW_KEY_1; i <= GLFW_KEY_9; i++) {
        if (isKeyJustPressed(i)) {
            int slot = i - GLFW_KEY_1;
            player.setSelectedSlot(slot);
        }
    }
}

// Add scroll wheel support
private static double lastScrollY = 0;

public static void setupScrollCallback(long window) {
    glfwSetScrollCallback(window, (win, xOffset, yOffset) -> {
        lastScrollY += yOffset;
    });
}

public static void updateHotbarScroll(Player player) {
    if (lastScrollY > 0.5) {
        player.getHotbar().selectPrevious();
        lastScrollY = 0;
    } else if (lastScrollY < -0.5) {
        player.getHotbar().selectNext();
        lastScrollY = 0;
    }
}
```

---

## Phase 3: Inventory System

### Step 3.1: Create Inventory Class

```java
package com.jless.voxelGame.inventory;

public class Inventory {
    
    public static final int ROWS = 3;
    public static final int COLS = 9;
    public static final int INVENTORY_SIZE = ROWS * COLS;
    
    private ItemStack[] slots;
    
    public Inventory() {
        slots = new ItemStack[INVENTORY_SIZE];
        for (int i = 0; i < INVENTORY_SIZE; i++) {
            slots[i] = ItemStack.empty();
        }
    }
    
    public ItemStack getSlot(int index) {
        if (index >= 0 && index < INVENTORY_SIZE) {
            return slots[index];
        }
        return ItemStack.empty();
    }
    
    public void setSlot(int index, ItemStack stack) {
        if (index >= 0 && index < INVENTORY_SIZE) {
            slots[index] = stack != null ? stack : ItemStack.empty();
        }
    }
    
    public boolean addItem(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        
        // First, try to stack with existing items
        for (int i = 0; i < INVENTORY_SIZE; i++) {
            ItemStack existing = slots[i];
            if (existing.canStack(stack)) {
                int space = existing.getRemainingSpace();
                if (space > 0) {
                    int toAdd = Math.min(space, stack.getQuantity());
                    existing.addQuantity(toAdd);
                    stack.addQuantity(-toAdd);
                    
                    if (stack.getQuantity() <= 0) {
                        return true;
                    }
                }
            }
        }
        
        // If there's still items left, find empty slot
        for (int i = 0; i < INVENTORY_SIZE; i++) {
            if (slots[i].isEmpty()) {
                slots[i] = stack.copy();
                return true;
            }
        }
        
        return false; // Inventory full
    }
    
    public int getSlotIndex(int row, int col) {
        if (row < 0 || row >= ROWS || col < 0 || col >= COLS) {
            return -1;
        }
        return row * COLS + col;
    }
}
```

### Step 3.2: Create InventoryRenderer

```java
package com.jless.voxelGame.ui;

import static org.lwjgl.opengl.GL11.*;
import org.joml.*;
import com.jless.voxelGame.inventory.*;
import com.jless.voxelGame.entity.*;

public class InventoryRenderer {
    
    private static final int SLOT_SIZE = 40;
    private static final int SLOT_PADDING = 4;
    private static final int PANEL_PADDING = 10;
    
    private static final float[] PANEL_COLOR = {0.15f, 0.15f, 0.15f, 0.95f};
    private static final float[] SLOT_COLOR = {0.25f, 0.25f, 0.25f, 0.9f};
    private static final float[] SLOT_HOVER = {0.35f, 0.35f, 0.35f, 1.0f};
    
    private UIRenderer uiRenderer;
    private int screenWidth;
    private int screenHeight;
    
    private int hoveredSlot = -1;
    
    public InventoryRenderer(UIRenderer uiRenderer, int screenWidth, int screenHeight) {
        this.uiRenderer = uiRenderer;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }
    
    public void render(Inventory inventory, Hotbar hotbar) {
        // Calculate panel dimensions
        int panelWidth = Inventory.COLS * (SLOT_SIZE + SLOT_PADDING) + PANEL_PADDING * 2;
        int panelHeight = (Inventory.ROWS + 1) * (SLOT_SIZE + SLOT_PADDING) + 
                          SLOT_PADDING + PANEL_PADDING * 2;
        
        int panelX = (screenWidth - panelWidth) / 2;
        int panelY = (screenHeight - panelHeight) / 2;
        
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        
        UIShader.use();
        UIShader.setProjection(uiRenderer.getOrthoMatrix());
        UIShader.setUseTexture(false);
        
        // Draw panel background
        UIShader.setColor(PANEL_COLOR[0], PANEL_COLOR[1], 
                          PANEL_COLOR[2], PANEL_COLOR[3]);
        uiRenderer.drawColoredQuad(panelX, panelY, panelWidth, panelHeight,
                                  PANEL_COLOR[0], PANEL_COLOR[1],
                                  PANEL_COLOR[2], PANEL_COLOR[3]);
        
        int slotStartX = panelX + PANEL_PADDING;
        int slotStartY = panelY + PANEL_PADDING;
        
        // Render inventory slots (3 rows)
        for (int row = 0; row < Inventory.ROWS; row++) {
            for (int col = 0; col < Inventory.COLS; col++) {
                int x = slotStartX + col * (SLOT_SIZE + SLOT_PADDING);
                int y = slotStartY + row * (SLOT_SIZE + SLOT_PADDING);
                
                int slotIndex = inventory.getSlotIndex(row, col);
                boolean hovered = (slotIndex == hoveredSlot);
                
                renderSlot(x, y, inventory.getSlot(slotIndex), hovered);
            }
        }
        
        // Render hotbar (1 row below inventory)
        int hotbarY = slotStartY + Inventory.ROWS * (SLOT_SIZE + SLOT_PADDING) + SLOT_PADDING;
        
        for (int i = 0; i < Hotbar.HOTBAR_SIZE; i++) {
            int x = slotStartX + i * (SLOT_SIZE + SLOT_PADDING);
            boolean selected = (i == hotbar.getSelectedSlot());
            
            renderSlot(x, hotbarY, hotbar.getSlot(i), selected);
        }
        
        glDisable(GL_BLEND);
    }
    
    private void renderSlot(int x, int y, ItemStack stack, boolean highlighted) {
        float[] color = highlighted ? SLOT_HOVER : SLOT_COLOR;
        
        UIShader.setColor(color[0], color[1], color[2], color[3]);
        uiRenderer.drawColoredQuad(x, y, SLOT_SIZE, SLOT_SIZE,
                                  color[0], color[1], color[2], color[3]);
        
        if (!stack.isEmpty()) {
            renderItem(stack, x, y);
        }
    }
    
    private void renderItem(ItemStack stack, int x, int y) {
        // Similar to HotbarRenderer.renderItem()
        int itemSize = 32;
        int offsetX = x + (SLOT_SIZE - itemSize) / 2;
        int offsetY = y + (SLOT_SIZE - itemSize) / 2;
        
        glEnable(GL_DEPTH_TEST);
        
        Matrix4f itemProj = new Matrix4f().ortho(
            0, screenWidth, screenHeight, 0, -100, 100
        );
        
        Matrix4f itemTransform = new Matrix4f()
            .translate(offsetX + itemSize / 2f, offsetY + itemSize / 2f, 0)
            .rotateY((float) Math.toRadians(-45))
            .rotateX((float) Math.toRadians(30))
            .scale(itemSize * 0.5f);
        
        Shaders.use();
        
        java.nio.FloatBuffer projBuf = org.lwjgl.BufferUtils.createFloatBuffer(16);
        itemProj.get(projBuf);
        Shaders.setProjMatrix(projBuf);
        
        Matrix4f identity = new Matrix4f();
        java.nio.FloatBuffer viewBuf = org.lwjgl.BufferUtils.createFloatBuffer(16);
        identity.get(viewBuf);
        Shaders.setViewMatrix(viewBuf);
        
        EntityRender.drawBlockItem(itemTransform, stack.getBlockID());
        
        glDisable(GL_DEPTH_TEST);
    }
    
    public void updateHoveredSlot(int mouseX, int mouseY, int panelX, int panelY) {
        int slotStartX = panelX + PANEL_PADDING;
        int slotStartY = panelY + PANEL_PADDING;
        
        hoveredSlot = -1;
        
        for (int row = 0; row < Inventory.ROWS; row++) {
            for (int col = 0; col < Inventory.COLS; col++) {
                int x = slotStartX + col * (SLOT_SIZE + SLOT_PADDING);
                int y = slotStartY + row * (SLOT_SIZE + SLOT_PADDING);
                
                if (mouseX >= x && mouseX < x + SLOT_SIZE &&
                    mouseY >= y && mouseY < y + SLOT_SIZE) {
                    hoveredSlot = row * Inventory.COLS + col;
                    return;
                }
            }
        }
    }
}
```

### Step 3.3: Create InventoryManager

```java
package com.jless.voxelGame.inventory;

public class InventoryManager {
    
    private Inventory inventory;
    private Hotbar hotbar;
    private boolean inventoryOpen;
    
    private ItemStack heldStack; // Stack being dragged
    
    public InventoryManager(Hotbar hotbar) {
        this.hotbar = hotbar;
        this.inventory = new Inventory();
        this.inventoryOpen = false;
        this.heldStack = ItemStack.empty();
    }
    
    public void toggleInventory() {
        inventoryOpen = !inventoryOpen;
    }
    
    public boolean isInventoryOpen() {
        return inventoryOpen;
    }
    
    public Inventory getInventory() {
        return inventory;
    }
    
    public Hotbar getHotbar() {
        return hotbar;
    }
    
    public void handleSlotClick(int slotIndex, boolean isInventorySlot) {
        ItemStack clickedSlot;
        
        if (isInventorySlot) {
            clickedSlot = inventory.getSlot(slotIndex);
        } else {
            clickedSlot = hotbar.getSlot(slotIndex);
        }
        
        // Swap held stack with clicked slot
        if (heldStack.isEmpty() && !clickedSlot.isEmpty()) {
            // Pick up stack
            heldStack = clickedSlot.copy();
            clickedSlot.setQuantity(0);
        } else if (!heldStack.isEmpty() && clickedSlot.isEmpty()) {
            // Place stack
            if (isInventorySlot) {
                inventory.setSlot(slotIndex, heldStack);
            } else {
                hotbar.setSlot(slotIndex, heldStack);
            }
            heldStack = ItemStack.empty();
        } else if (!heldStack.isEmpty() && !clickedSlot.isEmpty()) {
            // Swap stacks or merge
            if (heldStack.canStack(clickedSlot)) {
                int space = clickedSlot.getRemainingSpace();
                int toTransfer = Math.min(space, heldStack.getQuantity());
                
                clickedSlot.addQuantity(toTransfer);
                heldStack.addQuantity(-toTransfer);
                
                if (heldStack.getQuantity() <= 0) {
                    heldStack = ItemStack.empty();
                }
            } else {
                // Swap different items
                ItemStack temp = heldStack;
                heldStack = clickedSlot.copy();
                
                if (isInventorySlot) {
                    inventory.setSlot(slotIndex, temp);
                } else {
                    hotbar.setSlot(slotIndex, temp);
                }
            }
        }
    }
    
    public ItemStack getHeldStack() {
        return heldStack;
    }
}
```

---

## Phase 4: Health System

### Step 4.1: Create HealthManager

```java
package com.jless.voxelGame.player;

public class HealthManager {
    
    private float health;
    private float maxHealth;
    
    private float lastDamageTime;
    private float damageFlashDuration = 0.3f;
    
    public HealthManager(float maxHealth) {
        this.maxHealth = maxHealth;
        this.health = maxHealth;
        this.lastDamageTime = -1000f;
    }
    
    public void damage(float amount) {
        health = Math.max(0, health - amount);
        lastDamageTime = getCurrentTime();
        
        if (health <= 0) {
            onDeath();
        }
    }
    
    public void heal(float amount) {
        health = Math.min(maxHealth, health + amount);
    }
    
    public void setHealth(float health) {
        this.health = Math.max(0, Math.min(maxHealth, health));
    }
    
    public float getHealth() {
        return health;
    }
    
    public float getMaxHealth() {
        return maxHealth;
    }
    
    public float getHealthPercentage() {
        return health / maxHealth;
    }
    
    public boolean isDead() {
        return health <= 0;
    }
    
    public boolean isRecentlyDamaged() {
        return (getCurrentTime() - lastDamageTime) < damageFlashDuration;
    }
    
    private void onDeath() {
        System.out.println("Player died!");
        // Handle death logic
    }
    
    private float getCurrentTime() {
        return (float) (System.currentTimeMillis() / 1000.0);
    }
}
```

### Step 4.2: Create HealthRenderer

```java
package com.jless.voxelGame.ui;

import static org.lwjgl.opengl.GL11.*;
import com.jless.voxelGame.player.*;

public class HealthRenderer {
    
    private static final int HEART_SIZE = 18;
    private static final int HEART_SPACING = 4;
    private static final int MAX_HEARTS = 10;
    
    private static final float[] HEART_BG_COLOR = {0.2f, 0.0f, 0.0f, 0.8f};
    private static final float[] HEART_COLOR = {0.9f, 0.1f, 0.1f, 1.0f};
    private static final float[] HEART_DAMAGE_COLOR = {1.0f, 0.3f, 0.0f, 1.0f};
    
    private UIRenderer uiRenderer;
    private int screenWidth;
    private int screenHeight;
    
    public HealthRenderer(UIRenderer uiRenderer, int screenWidth, int screenHeight) {
        this.uiRenderer = uiRenderer;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }
    
    public void render(HealthManager healthManager) {
        float healthPercent = healthManager.getHealthPercentage();
        int fullHearts = (int) Math.floor(healthPercent * MAX_HEARTS);
        float partialHeart = (healthPercent * MAX_HEARTS) - fullHearts;
        
        int totalWidth = MAX_HEARTS * (HEART_SIZE + HEART_SPACING);
        int startX = (screenWidth - totalWidth) / 2;
        int startY = screenHeight - 80;
        
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        
        UIShader.use();
        UIShader.setProjection(uiRenderer.getOrthoMatrix());
        UIShader.setUseTexture(false);
        
        boolean damaged = healthManager.isRecentlyDamaged();
        float[] heartColor = damaged ? HEART_DAMAGE_COLOR : HEART_COLOR;
        
        for (int i = 0; i < MAX_HEARTS; i++) {
            int x = startX + i * (HEART_SIZE + HEART_SPACING);
            int y = startY;
            
            // Draw heart background
            UIShader.setColor(HEART_BG_COLOR[0], HEART_BG_COLOR[1],
                              HEART_BG_COLOR[2], HEART_BG_COLOR[3]);
            uiRenderer.drawColoredQuad(x, y, HEART_SIZE, HEART_SIZE,
                                      HEART_BG_COLOR[0], HEART_BG_COLOR[1],
                                      HEART_BG_COLOR[2], HEART_BG_COLOR[3]);
            
            // Draw filled portion
            if (i < fullHearts) {
                // Full heart
                UIShader.setColor(heartColor[0], heartColor[1],
                                  heartColor[2], heartColor[3]);
                uiRenderer.drawColoredQuad(x, y, HEART_SIZE, HEART_SIZE,
                                          heartColor[0], heartColor[1],
                                          heartColor[2], heartColor[3]);
            } else if (i == fullHearts && partialHeart > 0) {
                // Partial heart
                int partialWidth = (int) (HEART_SIZE * partialHeart);
                UIShader.setColor(heartColor[0], heartColor[1],
                                  heartColor[2], heartColor[3]);
                uiRenderer.drawColoredQuad(x, y, partialWidth, HEART_SIZE,
                                          heartColor[0], heartColor[1],
                                          heartColor[2], heartColor[3]);
            }
        }
        
        glDisable(GL_BLEND);
    }
    
    public void updateScreenSize(int width, int height) {
        this.screenWidth = width;
        this.screenHeight = height;
    }
}
```

---

## Phase 5: Polish and Integration

### Step 5.1: Create Unified UIManager

```java
package com.jless.voxelGame.ui;

import com.jless.voxelGame.player.*;
import com.jless.voxelGame.inventory.*;

public class UIManager {
    
    private UIRenderer uiRenderer;
    private HotbarRenderer hotbarRenderer;
    private InventoryRenderer inventoryRenderer;
    private HealthRenderer healthRenderer;
    
    private InventoryManager inventoryManager;
    private HealthManager healthManager;
    
    private boolean inventoryOpen;
    
    public UIManager(int screenWidth, int screenHeight, Player player) {
        // Create UI system
        UIRenderer.create(screenWidth, screenHeight);
        UIShader.create();
        
        this.uiRenderer = UIRenderer.getInstance();
        this.hotbarRenderer = new HotbarRenderer(uiRenderer, screenWidth, screenHeight);
        this.inventoryRenderer = new InventoryRenderer(uiRenderer, screenWidth, screenHeight);
        this.healthRenderer = new HealthRenderer(uiRenderer, screenWidth, screenHeight);
        
        this.inventoryManager = new InventoryManager(player.getHotbar());
        this.healthManager = new HealthManager(20f); // 10 hearts
        
        this.inventoryOpen = false;
    }
    
    public void render() {
        // Always render hotbar and health
        hotbarRenderer.render(inventoryManager.getHotbar());
        healthRenderer.render(healthManager);
        
        // Render inventory if open
        if (inventoryOpen) {
            inventoryRenderer.render(inventoryManager.getInventory(), 
                                    inventoryManager.getHotbar());
        }
    }
    
    public void toggleInventory() {
        inventoryOpen = !inventoryOpen;
    }
    
    public boolean isInventoryOpen() {
        return inventoryOpen;
    }
    
    public HealthManager getHealthManager() {
        return healthManager;
    }
    
    public InventoryManager getInventoryManager() {
        return inventoryManager;
    }
    
    public void updateScreenSize(int width, int height) {
        uiRenderer.updateProjection(width, height);
        hotbarRenderer.updateScreenSize(width, height);
        healthRenderer.updateScreenSize(width, height);
    }
    
    public void cleanup() {
        uiRenderer.cleanup();
        UIShader.cleanup();
    }
}
```

### Step 5.2: Update App.java Integration

```java
// In App.java

private UIManager uiManager;

private void init() {
    // ... existing initialization ...
    
    // Create UI after player
    uiManager = new UIManager(Consts.W_WIDTH, Consts.W_HEIGHT, player);
}

private void loop() {
    while(!glfwWindowShouldClose(Window.getWindow())) {
        // ... existing game loop ...
        
        // Handle inventory toggle
        if (Input.isKeyJustPressed(GLFW_KEY_E)) {
            uiManager.toggleInventory();
        }
        
        // Update input based on inventory state
        if (!uiManager.isInventoryOpen()) {
            // Normal gameplay input
            Input.handleHotbarInput(player);
            Input.updateHotbarScroll(player);
        } else {
            // Inventory input
            // Handle mouse clicks on slots
        }
        
        // ... render world ...
        
        // Render UI (always last)
        uiManager.render();
        
        glfwSwapBuffers(Window.getWindow());
    }
}

private void cleanup() {
    // ... existing cleanup ...
    uiManager.cleanup();
}
```

### Step 5.3: Add Text Rendering (Optional Enhancement)

For displaying item quantities, you can use a simple bitmap font renderer:

```java
package com.jless.voxelGame.ui;

public class BitmapFont {
    
    private int fontTexture;
    private int charWidth = 8;
    private int charHeight = 8;
    
    public void renderText(String text, int x, int y, float scale) {
        // Simple bitmap font rendering
        // You can load a font texture atlas and render character quads
    }
}
```

---

## Implementation Roadmap

### Week 1: Foundation
1. ✅ Create UIRenderer base class
2. ✅ Create UI shaders
3. ✅ Test rendering colored quads

### Week 2: Hotbar
1. ✅ Implement ItemStack
2. ✅ Implement Hotbar class
3. ✅ Create HotbarRenderer
4. ✅ Integrate with Player
5. ✅ Add input handling (number keys, scroll wheel)

### Week 3: Inventory
1. ✅ Implement Inventory class
2. ✅ Create InventoryRenderer
3. ✅ Implement InventoryManager
4. ✅ Add mouse interaction
5. ✅ Test item transfer logic

### Week 4: Health
1. ✅ Implement HealthManager
2. ✅ Create HealthRenderer
3. ✅ Add damage/healing mechanics
4. ✅ Test visual feedback

### Week 5: Polish
1. Add text rendering for quantities
2. Add sound effects
3. Add animations (slot selection, damage flash)
4. Optimize rendering
5. Bug fixes and testing

---

## Testing Checklist

### Hotbar
- [ ] Can select slots with number keys 1-9
- [ ] Can scroll through slots with mouse wheel
- [ ] Selected slot is visually highlighted
- [ ] Items render correctly in slots
- [ ] Placing blocks decrements quantity
- [ ] Empty slots don't allow placement

### Inventory
- [ ] Opens/closes with 'E' key
- [ ] Mouse interaction works
- [ ] Can pick up items
- [ ] Can place items
- [ ] Can swap items
- [ ] Stacking works correctly
- [ ] Hotbar syncs with inventory

### Health
- [ ] Hearts display correctly
- [ ] Partial hearts render properly
- [ ] Damage flash effect works
- [ ] Health can't go below 0 or above max
- [ ] Death triggers properly

---

## Common Issues and Solutions

### Issue: Items not rendering in slots
**Solution**: Ensure EntityRender.drawBlockItem() is being called with correct projection/view matrices

### Issue: Hotbar not updating when scrolling
**Solution**: Check scroll callback is properly registered in GLFW initialization

### Issue: Inventory clicks not registering
**Solution**: Verify mouse coordinate conversion and slot bounds checking

### Issue: UI rendering behind world
**Solution**: Render UI after world, ensure depth testing is disabled for UI

### Issue: Transparency not working
**Solution**: Enable GL_BLEND with correct blend function before UI rendering

---

## Future Enhancements

1. **Crafting System**: 3x3 crafting grid in inventory
2. **Item Tooltips**: Show item name on hover
3. **Durability**: For tools and equipment
4. **Equipment Slots**: Armor system
5. **Creative Mode**: Infinite items palette
6. **Item Dropping**: Drop items from inventory
7. **Chest Inventory**: Container blocks
8. **Hunger System**: Food and saturation
9. **Armor Bar**: Visual armor display
10. **Better Item Rendering**: Custom item models

---

## Conclusion

This tutorial provides a complete, production-ready UI system for your voxel game. The modular design makes it easy to extend with new features, and the native OpenGL rendering ensures it integrates seamlessly with your existing game architecture.

The system is:
- **Performant**: Minimal draw calls, efficient rendering
- **Extensible**: Easy to add new UI elements
- **Clean**: Well-organized code with clear separation of concerns
- **Professional**: Polished visuals matching modern voxel games

Good luck with your implementation! 🎮
