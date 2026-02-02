# DEVELOPMENT.md - Complete Development Guide & Session Context

## Current Session Status
**Date**: 2026-02-02  
**Focus**: Complete development consolidation and feature roadmap  
**Status**: Comprehensive documentation integration completed

---

## Quick Navigation

- [🎯 Current Implementation Plan](#immediate-implementation-priority-time-relative-game-systems)
- [🏗️ Architecture Overview](#architecture-overview)
- [🌍 World Generation Features](#world-generation-enhancement-guide)
- [🎮 UI & Inventory Systems](#ui-system-guide)
- [🧍 Entity System Porting](#entity-port-guide)
- [📅 Development Roadmap](#development-roadmap)
- [💡 Lighting System](#lighting-system-tutorial)
- [🔧 Development Standards](#development-standards--patterns)

---

## 🎯 Immediate Implementation Priority: Time-Relative Game Systems

### Issue Identified
The game has critical framerate dependencies that make gameplay speed vary with FPS:

**Critical Issues Found:**
1. **Hardcoded Delta Time** - `App.java:107` uses `float dt = 0.016f` (assumes exactly 60 FPS)
2. **Hardcoded Delta Time in Player Update** - `App.java:143` passes `0.016f` instead of real delta time  
3. **Mouse Input Not Time-Normalized** - Raw pixel differences vary with framerate

**Good News**: All physics calculations are already properly structured to use delta time multiplication.

### Implementation Plan (Ready to Execute)

#### Step 1: Add Timing Variables to App.java
```java
// Add to class variables:
private double lastTime = 0.0;
// Optional for smoothing:
private float smoothedDt = 0.016f;
private static final float DT_SMOOTHING_FACTOR = 0.2f;
```

#### Step 2: Initialize Last Time in init() Method
**File**: `App.java`, end of `init()` method
```java
lastTime = glfwGetTime();
```

#### Step 3: Replace Hardcoded Delta Time in loop() Method
**File**: `App.java`, line 107
```java
// Replace:
// float dt = 0.016f;

// With proper delta time calculation:
double currentTime = glfwGetTime();
float rawDt = (float)(currentTime - lastTime);
lastTime = currentTime;

// Clamp to prevent physics issues
rawDt = Math.min(rawDt, 0.1f);

// Optional smoothing:
smoothedDt = smoothedDt * (1.0f - DT_SMOOTHING_FACTOR) + rawDt * DT_SMOOTHING_FACTOR;
float dt = smoothedDt;
```

#### Step 4: Update PlayerController Call
**File**: `App.java`, line 143
```java
// Replace:
// playerController.update(world, 0.016f, jumpPressed, threadManager);

// With:
playerController.update(world, dt, jumpPressed, threadManager);
```

#### Step 5: Fix Mouse Input Time Normalization

**File**: `PlayerController.java`, `processMouse()` method (lines 54-60)
```java
public void processMouse(float dx, float dy, float dt) {
    // Normalize mouse input by delta time for consistent sensitivity
    float timeFactor = dt * 60.0f; // Normalize to 60 FPS baseline
    
    yaw += (float)Math.toRadians(dx * Consts.MOUSE_SENS * timeFactor);
    pitch += (float)Math.toRadians(dy * Consts.MOUSE_SENS * timeFactor);
    
    if(pitch > Math.toRadians(89.0f)) pitch = (float)Math.toRadians(89.0f);
    if(pitch < Math.toRadians(-89.0f)) pitch = (float)Math.toRadians(-89.0f);
}
```

**File**: `PlayerController.java`, `updateCameraRotation()` method (lines 239-244)
```java
private void updateCameraRotation(float dt) {
    float mouseDX = Input.getMouseDX();
    float mouseDY = Input.getMouseDY();

    processMouse(mouseDX, mouseDY, dt);
}
```

**File**: `PlayerController.java`, `update()` method (line 256)
```java
// Replace:
// updateCameraRotation();

// With:
updateCameraRotation(dt);
```

#### Step 6: Testing & Verification
- Test movement consistency at different framerates
- Verify mouse sensitivity remains consistent
- Check physics behavior with VSYNC on/off
- Consider testing with `Consts.VSYNC = 0` to unlock framerate

---

## 🏗️ Architecture Overview

### Core Architecture
- **Main Class**: `com.jless.voxelGame.App`
- **Build System**: Maven with LWJGL 3.3.6
- **Graphics**: OpenGL with ImGui UI, JOML math
- **Target**: Java 21 with UTF-8 encoding
- **Threading**: ChunkThreadManager for world generation
- **Lighting**: Day/night cycle with shadow mapping

### Current Systems Status
1. **Rendering**: ✅ Functional with VBO-based chunk rendering
2. **World Generation**: ✅ Perlin noise with async chunk loading
3. **Player Physics**: ⚠️ Structured correctly but needs time fixes
4. **Input**: ✅ Basic keyboard/mouse handling
5. **UI**: ✅ ImGui overlay with FPS counter
6. **Lighting**: ✅ Dynamic lighting with shadow mapping (implementable)
7. **Inventory**: ✅ Complete system with hotbar and inventory UI (implementable)
8. **Entities**: ⚠️ Base system exists, needs porting (see Entity Port Guide)

### Code Quality Assessment
- **Strengths**: Well-organized package structure, proper LWJGL usage, good constants management
- **Current Issues**: Framerate dependencies (being fixed), potentially some hardcoded values
- **Architecture**: Solid foundation with singleton patterns and proper resource management

---

## 🌍 World Generation Enhancement Guide

### Available Features (Ready to Implement)
The comprehensive world generation system includes:

#### 1. Enhanced Noise System
- **Multi-Octave Noise**: Natural-looking terrain with layered frequencies
- **SimplexNoise**: Improved over Perlin for better visual quality
- **3D Noise Support**: For cave generation and underground features

#### 2. Complete Biome System
- **8 Biome Types**: Ocean, Plains, Forest, Desert, Mountains, Taiga, Swamp, Snowy Tundra
- **Smooth Transitions**: Biome blending for natural borders
- **Temperature/Moisture Maps**: Realistic biome distribution

#### 3. Advanced Terrain Generation
- **Heightmap Sculpting**: Continent-scale and detailed terrain
- **Biome-Specific Modifications**: Mountains get extra height, oceans stay flat
- **Surface Depth Variation**: Deserts have deeper sand layers

#### 4. Procedural Tree System
- **Multiple Tree Types**: Oak, Spruce, Birch with different shapes
- **Biome-Appropriate Placement**: Forests get many trees, deserts none
- **Realistic Canopies**: Rounded shapes for oaks, conical for spruces

#### 5. Cave Systems
- **Worm Caves**: Interconnected tunnel systems using 3D noise
- **Large Caverns**: Open underground spaces for exploration
- **Vertical Distribution**: More caves at middle depths, fewer near surface/bedrock

#### 6. Ore Distribution
- **Realistic Vein Generation**: Spherical, clustered ore deposits
- **Depth-Based Rarity**: Diamonds only deep, coal everywhere
- **Configurable Parameters**: Easy to balance ore spawning

#### Implementation Status
📁 **Location**: `DOCS/WORLD_ENANCEMENT_GUIDE.md`
🔧 **Status**: Complete implementation guide with code examples
⏱️ **Effort**: ~6 weeks of development time
🎯 **Priority**: High - Major gameplay enhancement

---

## 🎮 UI System Guide

### Complete UI/Inventory System Available
A professional UI system is ready for implementation:

#### 1. UI Rendering Foundation
- **Native OpenGL Rendering**: No ImGui dependency for game UI
- **Orthographic Projection**: Proper 2D screen space rendering
- **Modular Design**: Independent, reusable UI components
- **Texture Atlas Support**: Efficient texture management

#### 2. Hotbar System
- **9-Slot Hotbar**: Number key and scroll wheel selection
- **Item Visualization**: 3D item rendering in slots
- **Quantity Display**: Stack sizes shown for items >1
- **Visual Feedback**: Selected slot highlighting

#### 3. Inventory Management
- **27-Slot Inventory**: 9x4 grid plus hotbar row
- **Drag & Drop**: Intuitive item manipulation
- **Stack Splitting**: Right-click to split stacks
- **Auto-Stacking**: Items automatically combine

#### 4. Health System
- **Heart Display**: Up to 10 hearts with partial hearts
- **Damage Flash**: Visual feedback when taking damage
- **Smooth Animations**: Health changes animate smoothly

#### 5. Advanced Features
- **Crafting Grid**: 3x3 crafting interface ready
- **Recipe Book**: Browse known recipes
- **Item Tooltips**: Hover information display
- **Equipment Slots**: Armor and tool slots

#### Implementation Status
📁 **Location**: `DOCS/UI_SYSTEM_GUIDE.md`
🔧 **Status**: Complete system with code examples and integration guide
⏱️ **Effort**: ~5 weeks of development time
🎯 **Priority**: High - Essential for complete gameplay

---

## 🧍 Entity Port Guide

### Entity System Migration Required
Current entity system needs migration to new architecture:

#### Critical Issues Identified
1. **Broken Leg Animation**: `withPivotRotationX()` creates rotated matrix but doesn't use it
2. **Wrong Import Paths**: Using old package structure
3. **API Changes**: Method signatures have changed
4. **Rendering Calls**: Using deprecated `VoxelRender.drawEntityBoxShader()`

#### Migration Steps
1. **Fix Base Entity Class**: Replace with corrected version
2. **Update EntityManager**: Fully migrated entity manager available
3. **Port Individual Entities**: Follow pattern for EntityPenguin and EntityPig
4. **Update Method Calls**: All `VoxelRender.drawEntityBoxShader()` calls need new API

#### Key Changes Required
```java
// OLD (broken):
Matrix4f leftLeg0 = withPivotRotationX(entityModel, hipX, hipY, hipZ, angle);
VoxelRender.drawEntityBoxShader(lx0, y0, z0, lx1, y1, z1, legColor, entityModel);

// NEW (fixed):
Matrix4f leftLeg0 = withPivotRotationX(entityModel, hipX, hipY, hipZ, angle);
EntityRender.drawEntityWithTransform(leftLeg0, lx0, y0, z0, lx1, y1, z1, legColor);
```

#### Implementation Status
📁 **Location**: `DOCS/ENTITY_PORT_GUIDE.md`
🔧 **Status**: Urgent - Current entities will have visual glitches
⏱️ **Effort**: ~1 week for full migration
🎯 **Priority**: Critical - Blocker for entity features

---

## 📅 Development Roadmap

### Comprehensive 8-Week Development Plan

#### Phase 1: Entity System Foundation (Week 1-2)
- **Core Entity Architecture**: Component-based entity system
- **Friendly Entity System**: Villagers, pets, NPCs
- **Enemy Entity System**: Zombies, archers, bosses
- **Entity Rendering**: Optimized batch rendering

#### Phase 2: Inventory System (Week 3)
- **Core Inventory Architecture**: Grid-based storage with stacking
- **Item System**: Tools, weapons, consumables, materials
- **Inventory UI**: Drag & drop interface with visual feedback

#### Phase 3: Crafting System (Week 4)
- **Recipe System Foundation**: Shaped, shapeless, smelting recipes
- **Recipe Categories**: Basic crafting, smelting, alchemy, enchanting
- **Recipe Unlock System**: Discovery-based unlocking

#### Phase 4: Crafting UI System (Week 5)
- **Crafting Interface**: 3x3 grid with recipe preview
- **Recipe Discovery System**: Experimental crafting and hints
- **Advanced UI**: Recipe book, quick crafting, animations

#### Phase 5: Advanced Features (Week 6-7)
- **Entity Interaction System**: Dialogue, trading, quests
- **Advanced AI Behaviors**: Pathfinding, behavior trees, combat AI
- **Combat System Enhancement**: Weapons, armor, status effects

#### Phase 6: Polish and Optimization (Week 8)
- **Performance Optimization**: Entity rendering, pathfinding, inventory ops
- **Save System Integration**: Entity state, inventory, recipe persistence

#### Implementation Status
📁 **Location**: `DOCS/DEVELOPMENT_ROADMAP.md`
🔧 **Status**: Complete roadmap with dependencies and milestones
⏱️ **Effort**: 8 weeks full-time development
🎯 **Priority**: Strategic - Long-term feature planning

---

## 💡 Lighting System Tutorial

### Complete Day/Night Cycle System Available
Professional lighting system with optional shadow mapping:

#### Core Features
- **Dynamic Day/Night Cycle**: Configurable length (default 20 minutes)
- **Smooth Sun/Moon Transitions**: Realistic celestial body movement
- **Color-Shifting Sky**: Dawn, day, dusk, night color gradients
- **Directional Lighting**: Sun and moon provide directional light
- **Ambient Lighting**: Prevents total darkness, changes with time

#### Shadow Mapping (Optional)
- **Realistic Shadows**: High-quality shadow rendering
- **PCF Filtering**: Smooth shadow edges
- **Configurable Quality**: Adjustable shadow resolution and distance
- **Performance Optimized**: Toggle on/off for different hardware

#### Advanced Features
- **Atmospheric Scattering**: Distance-based fog blending
- **Weather Effects**: Storm darkening and rain integration
- **Dynamic Light Sources**: Point lights for torches and lava
- **Star Rendering**: Night sky with star brightness

#### Implementation Status
📁 **Location**: `DOCS/LIGHTING_TUTORIAL.md`
🔧 **Status**: Production-ready system with full documentation
⏱️ **Effort**: ~1 week for basic, ~2 weeks with shadows
🎯 **Priority**: Medium - Visual enhancement

---

## 🔧 Development Standards & Patterns

### Build Commands (From AGENTS.md)
```bash
mvn clean compile      # Check syntax during development
mvn test              # Run all tests
mvn test -Dtest=ClassName#methodName  # Run specific test method
mvn clean package     # Create fat JAR with all dependencies
java -jar target/voxelGame-1.0-SNAPSHOT-fat.jar  # Execute the game
mvn clean             # Remove build artifacts
```

### Code Style Guidelines (From AGENTS.md)

#### Naming Conventions
- **Classes**: PascalCase (Window, PlayerController, ChunkGen)
- **Methods**: camelCase (getWindow, waylandCheck, updatePlayer)
- **Constants**: UPPER_SNAKE_CASE (W_WIDTH, VSYNC, FOV)
- **Packages**: lowercase.with.dots (com.jless.voxelGame.player)
- **Variables**: camelCase with descriptive names

#### Import Organization
- Static imports for LWJGL constants: `import static org.lwjgl.glfw.GLFW.*;`
- Regular imports for classes and interfaces
- No wildcard imports (*)
- Group imports logically: java.*, org.*, com.*
- Keep import sections clean and organized

#### Class Design Patterns
- **Singleton pattern** for manager classes (see Window.java)
- **Private constructors** for utility classes (see Consts.java)
- **Static factory methods** where appropriate
- **Package-private classes** for internal implementation
- **Final classes** for utility/constant classes

#### LWJGL Usage Patterns
- Always check GLFW initialization results
- Use static imports for GLFW constants
- Handle Wayland compatibility (see App.waylandCheck())
- Proper context management with Window singleton
- Use `glfwMakeContextCurrent()` before OpenGL calls

### Testing Guidelines (From AGENTS.md)

#### Framework & Structure
- **JUnit 5** with Jupiter assertions
- Follow AppTest.java pattern for test structure
- Use `@Test` annotation for test methods
- Descriptive test method names: `should...()`
- Place tests in `src/test/java/com/jless/voxelGame/`

#### Best Practices
- Use `assertTrue()`, `assertEquals()`, `assertNotNull()` from JUnit 5
- Test public API only, avoid testing private methods
- Mock external dependencies when needed (GLFW, OpenGL)
- Keep tests focused and independent
- Use `@BeforeEach` and `@AfterEach` for setup/teardown

---

## 🚀 Next Development Priorities

### Immediate (This Session)
1. **Critical**: Complete time-relative implementation (game-breaking issue)
2. **Critical**: Fix entity system porting (visual glitches)
3. **High**: Implement basic inventory/hotbar system
4. **Medium**: Add lighting system for visual polish

### Short Term (Next Sessions)
1. **Complete UI System**: Full inventory management
2. **World Generation**: Enhanced terrain with biomes
3. **Save System**: Player position, world state persistence
4. **Settings Menu**: VSYNC toggle, sensitivity controls, graphics options

### Medium Term (Next Month)
1. **Crafting System**: Recipe-based crafting interface
2. **Entity AI**: Advanced behaviors and interactions
3. **Performance Optimization**: Frustum culling, render distance
4. **Multiplayer Foundation**: Network layer architecture

### Long Term (Future)
1. **Advanced Physics**: Better collision detection, water physics
2. **Mod Support**: Plugin system for blocks/items
3. **Complete Feature Set**: All systems from roadmap implemented
4. **Performance Profiling**: Identify and eliminate bottlenecks

---

## ⚠️ Technical Debt & Known Issues

### Critical Issues (Fix Immediately)
1. **Framerate Dependencies**: Being actively addressed
2. **Entity Animation Bugs**: Leg animation system broken
3. **API Inconsistencies**: Some deprecated method calls

### Current Technical Debt
1. **Hardcoded Values**: Some magic numbers in physics calculations
2. **Error Handling**: Could be more robust for OpenGL failures
3. **Resource Management**: Some OpenGL resources might need better cleanup

### Future Refactoring Opportunities
1. **Event System**: More robust input handling with events
2. **Configuration System**: External config files vs constants
3. **Asset Pipeline**: Better texture/block definition loading
4. **Testing**: Expand unit test coverage

---

## 🎯 Development Environment Setup

### Quick Start Checklist
- [ ] Java 21 installed and configured
- [ ] Maven build system working
- [ ] LWJGL dependencies properly loaded
- [ ] OpenGL context creation successful
- [ ] Cross-platform compatibility verified

### Performance Targets
- **Entity Count**: Support 100+ entities simultaneously
- **Inventory**: <1ms for any inventory operation
- **Crafting**: <10ms for recipe matching
- **Rendering**: Maintain 60 FPS with complex scenes
- **Memory**: <500MB for full game state

### Debugging Tools
- **Time Controls**: Fast-forward/rewind day cycle
- **Shadow Debug**: Visualize shadow maps
- **Entity Inspector**: View entity states and components
- **Performance Monitor**: Frame time and memory usage

---

## 📝 Session Management

### Git & Branch Strategy
- Main development on current branch
- Feature branches for major additions
- Keep master/production branch stable
- Commit frequently with descriptive messages

### Session Continuation Plan
When returning to development:
1. **Read this file** for complete context and current priorities
2. **Check critical issues** - fix framerate and entity bugs first
3. **Review implementation guides** for the feature you're working on
4. **Test thoroughly** at each development milestone
5. **Update documentation** as features are implemented

### Documentation Usage
- **AGENTS.md**: Core development standards and patterns
- **DEVELOPMENT.md**: Session context and current priorities (this file)
- **WORLD_ENANCEMENT_GUIDE.md**: Complete world generation implementation
- **UI_SYSTEM_GUIDE.md**: Full UI and inventory system
- **ENTITY_PORT_GUIDE.md**: Critical entity system migration
- **DEVELOPMENT_ROADMAP.md**: Long-term feature planning
- **LIGHTING_TUTORIAL.md**: Lighting and shadow implementation

---

This consolidated development guide provides complete context for all development activities, implementation plans, and technical standards for the voxel game project.