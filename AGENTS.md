# AGENTS.md - Development Guide for AI Assistants

## Project Overview
Java 21 voxel game using LWJGL 3.3.6 with Maven build system. OpenGL rendering with ImGui UI and JOML math library. Main class: `com.jless.voxelGame.App`.

## Build Commands

### Core Commands
- **Build**: `mvn clean compile` - Compiles the project
- **Test**: `mvn test` - Runs all tests
- **Single Test**: `mvn test -Dtest=ClassName#methodName` - Runs specific test method
- **Package**: `mvn clean package` - Creates fat JAR with all dependencies
- **Run**: `java -jar target/voxelGame-1.0-SNAPSHOT-fat.jar` - Execute the game
- **Clean**: `mvn clean` - Removes build artifacts

### Development Workflow
- Use `mvn compile` frequently to check syntax during development
- Run tests with `mvn test` after significant changes
- Package with assembly plugin for distribution/testing
- Fat JAR includes all native libraries for cross-platform deployment

## Code Style Guidelines

### Naming Conventions
- **Classes**: PascalCase (Window, PlayerController, ChunkGen)
- **Methods**: camelCase (getWindow, waylandCheck, updatePlayer)
- **Constants**: UPPER_SNAKE_CASE (W_WIDTH, VSYNC, FOV)
- **Packages**: lowercase.with.dots (com.jless.voxelGame.player)
- **Variables**: camelCase with descriptive names

### Import Organization
- Static imports for LWJGL constants: `import static org.lwjgl.glfw.GLFW.*;`
- Regular imports for classes and interfaces
- No wildcard imports (*)
- Group imports logically: java.*, org.*, com.*
- Keep import sections clean and organized

### Class Design Patterns
- **Singleton pattern** for manager classes (see Window.java)
- **Private constructors** for utility classes (see Consts.java)
- **Static factory methods** where appropriate
- **Package-private classes** for internal implementation
- **Final classes** for utility/constant classes

### Error Handling
- `IllegalStateException` for invalid state conditions
- `RuntimeException` for external library failures
- Null checks with descriptive error messages
- Proper resource cleanup for OpenGL objects
- Always check GLFW/LWJGL function return values

## Project-Specific Patterns

### LWJGL Usage
- Always check GLFW initialization results
- Use static imports for GLFW constants
- Handle Wayland compatibility (see App.waylandCheck())
- Proper context management with Window singleton
- Use `glfwMakeContextCurrent()` before OpenGL calls

### OpenGL Context
- Follow Window.java pattern for setup
- Use `GL.createCapabilities()` after context is current
- Handle VSYNC via `Consts.VSYNC`
- Cross-platform native library loading via Maven
- Proper error checking with `glGetError()`

### Constants Management
- Store all configuration in Consts.java
- Use descriptive names (W_WIDTH, not WW)
- Group related constants with comments
- Access constants statically: `Consts.W_WIDTH`
- Keep magic numbers out of game logic

## Testing Guidelines

### Framework & Structure
- **JUnit 5** with Jupiter assertions
- Follow AppTest.java pattern for test structure
- Use `@Test` annotation for test methods
- Descriptive test method names: `should...()`
- Place tests in `src/test/java/com/jless/voxelGame/`

### Best Practices
- Use `assertTrue()`, `assertEquals()`, `assertNotNull()` from JUnit 5
- Test public API only, avoid testing private methods
- Mock external dependencies when needed (GLFW, OpenGL)
- Keep tests focused and independent
- Use `@BeforeEach` and `@AfterEach` for setup/teardown

### Test Examples
```java
@Test
public void shouldCreateWindowSuccessfully() {
    Window.create(Consts.W_WIDTH, Consts.W_HEIGHT, "Test");
    long windowId = Window.getWindow();
    assertTrue(windowId != NULL);
}
```

## Development Notes

### Java Configuration
- **Target Java 21** (compiler configuration)
- Update Maven release setting from 17 to 21 for consistency
- **UTF-8 encoding** everywhere (configured in pom.xml)
- Use modern Java features where appropriate (records, streams)

### Dependencies
- **LWJGL 3.3.6** ecosystem for graphics and input
- **JOML 1.10.8** for math operations (vectors, matrices)
- **ImGui 1.86.11** for UI rendering
- **Cross-platform natives** automatically included via Maven
- **Steamworks4j** for Steam integration (if needed)

### Package Organization
```
com.jless.voxelGame/
├── App.java (main entry point)
├── Window.java (GLFW window management)
├── Consts.java (game constants)
├── Input.java (input handling)
├── player/ (player-related classes)
│   ├── Player.java
│   └── PlayerController.java
├── chunkGen/ (chunk generation algorithms)
└── worldGen/ (world generation systems)
```

## Common Tasks

### Adding New Game Systems
1. Create package under `com.jless.voxelGame`
2. Follow singleton pattern for manager classes
3. Add constants to `Consts.java` if needed
4. Write unit tests following `AppTest` pattern
5. Update build configuration if new dependencies required

### OpenGL Development
1. Always check for OpenGL errors with `glGetError()`
2. Proper cleanup of resources (buffers, shaders, textures)
3. Use VSYNC setting from `Consts.VSYNC`
4. Handle Wayland compatibility in initialization
5. Follow existing shader loading patterns in resources

### Cross-Platform Considerations
- Native libraries auto-extracted by Maven dependency plugin
- Wayland support via `glfwInitHint()` in App.java
- Test on multiple platforms when possible
- Use platform-agnostic file paths for resources

### Performance Guidelines
- Use vertex buffer objects (VBOs) for efficient rendering
- Implement frustum culling for chunk visibility
- Cache frequently accessed values
- Profile with Java VisualVM or similar tools
- Consider object pooling for frequently created objects

### Timing and Game Loop
- **CRITICAL**: Never use hardcoded delta time values
- Always measure real frame time with `glfwGetTime()`
- Use `float dt` for all physics and movement calculations
- Clamp delta time to prevent physics issues (max 0.1f recommended)
- Normalize mouse input by delta time for consistent sensitivity
- Consider frame time smoothing for smoother gameplay

### Time-Relative Implementation Pattern
```java
// In game loop:
double currentTime = glfwGetTime();
float dt = (float)(currentTime - lastTime);
lastTime = currentTime;

// Clamp and optionally smooth:
dt = Math.min(dt, 0.1f);
// smoothedDt = smoothedDt * (1.0f - factor) + dt * factor;

// Use dt in all updates:
playerController.update(world, dt, jumpPressed, threadManager);
lighting.update(dt);

// Mouse input normalization:
float timeFactor = dt * 60.0f; // Normalize to 60 FPS baseline
yaw += mouseDX * MOUSE_SENS * timeFactor;
```

## Important Reminders
- Never commit secrets or API keys
- Follow existing code patterns and conventions
- Test thoroughly before committing changes
- Document complex algorithms with clear comments
- Keep game loop performance in mind when adding features
- Always test gameplay at different framerates (30, 60, 144+ FPS)
- Verify mouse sensitivity consistency across framerates
- Use DEVELOPMENT.md for session-specific context and plans