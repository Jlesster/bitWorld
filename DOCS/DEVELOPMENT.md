# DEVELOPMENT.md - Current Session Context & Implementation Plan

## Current Session Status
**Date**: 2026-01-31  
**Focus**: Converting framerate-dependent game systems to time-relative implementation  
**Status**: Analysis complete, implementation planned, ready to execute

---

## Immediate Implementation Priority: Time-Relative Game Systems

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

## Project Context & Current State

### Architecture Overview
- **Main Class**: `com.jless.voxelGame.App`
- **Build System**: Maven with LWJGL 3.3.6
- **Graphics**: OpenGL with ImGui UI, JOML math
- **Target**: Java 21 with UTF-8 encoding
- **Threading**: ChunkThreadManager for world generation
- **Lighting**: Day/night cycle with shadow mapping

### Key Systems Status
1. **Rendering**: Functional with VBO-based chunk rendering
2. **World Generation**: Perlin noise with async chunk loading
3. **Player Physics**: Structured correctly but needs time fixes
4. **Input**: Basic keyboard/mouse handling
5. **UI**: ImGui overlay with FPS counter
6. **Lighting**: Dynamic lighting with shadow mapping

### Code Quality Assessment
- **Strengths**: Well-organized package structure, proper LWJGL usage, good constants management
- **Current Issues**: Framerate dependencies (being fixed), potentially some hardcoded values
- **Architecture**: Solid foundation with singleton patterns and proper resource management

---

## Previous Conversations Summary

### Session 1: Initial Codebase Familiarization
- Explored basic structure and LWJGL setup
- Identified build system and dependencies
- Confirmed project follows good LWJGL patterns
- Verified cross-platform compatibility setup

### Session 2: Development Standards & Patterns
- Reviewed AGENTS.md development guide
- Confirmed build commands and testing approach
- Validated code style conventions
- Identified proper Maven configuration

### Session 3 (Current): Framerate Dependency Analysis
- Comprehensive analysis of timing systems
- Identified critical framerate dependencies
- Created detailed implementation plan
- Prepared complete tutorial for time-relative fixes

---

## Next Development Priorities

### Immediate (This Session)
1. Complete time-relative implementation
2. Test thoroughly at different framerates
3. Verify mouse sensitivity consistency

### Short Term (Next Sessions)
1. **Sound System Integration**: Consider adding OpenAL for audio
2. **Save System**: Player position, world state persistence
3. **Settings Menu**: VSYNC toggle, sensitivity controls, graphics options
4. **Optimization**: Frustum culling improvements, render distance optimization

### Medium Term
1. **Advanced Physics**: Better collision detection, water physics
2. **Multiplayer Foundation**: Network layer architecture
3. **Mod Support**: Plugin system for blocks/items
4. **Performance Profiling**: Identify bottlenecks, optimize rendering

---

## Technical Debt & Known Issues

### Current Technical Debt
1. **Framerate Dependencies**: Being actively addressed
2. **Hardcoded Values**: Some magic numbers in physics calculations
3. **Error Handling**: Could be more robust for OpenGL failures
4. **Resource Management**: Some OpenGL resources might need better cleanup

### Future Refactoring Opportunities
1. **Event System**: More robust input handling with events
2. **Configuration System**: External config files vs constants
3. **Asset Pipeline**: Better texture/block definition loading
4. **Testing**: Expand unit test coverage

---

## Development Environment Notes

### Build Commands Reminder
```bash
mvn clean compile      # Check syntax during development
mvn test              # Run tests
mvn clean package     # Create fat JAR
java -jar target/voxelGame-1.0-SNAPSHOT-fat.jar  # Run game
```

### Testing Approach
- Use JUnit 5 following AppTest.java patterns
- Test public API only, mock external dependencies
- Focus on integration tests for game systems

### Code Style Reminders
- Static imports for LWJGL constants
- PascalCase classes, camelCase methods, UPPER_SNAKE_CASE constants
- No wildcard imports, clean import organization
- Follow singleton pattern for managers

---

## Git & Branch Strategy
- Main development on current branch
- Feature branches for major additions
- Keep master/production branch stable
- Commit frequently with descriptive messages

---

## Session Continuation Plan
When moving to PC:
1. Read DEVELOPMENT.md for full context
2. Review implementation plan above
3. Execute time-relative fixes step by step
4. Test changes thoroughly
5. Continue with next priority items

This file serves as complete session context and implementation guide.