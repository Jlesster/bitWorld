# Entity Migration Package - Quick Start

This package contains everything you need to port your entity system to the new project structure.

## What's Included
```
entity_migration/
├── ENTITY_PORTING_GUIDE.md     # Comprehensive migration guide
├── QUICK_START.md              # This file
└── entity/
    ├── Entity.java             # Fixed base class with proper pivot rotation
    └── EntityManager.java      # Fully migrated entity manager
```

## Installation Steps

### Step 1: Fix the Base Entity Class
Replace your current `entity/Entity.java` with the fixed version:
```bash
cp entity/Entity.java <your-project>/src/main/java/com/jless/voxelGame/entity/
```

**Why?** The current version has a broken `withPivotRotationX` method that will cause leg animation glitches.

### Step 2: Install EntityManager
Copy the migrated EntityManager:
```bash
cp entity/EntityManager.java <your-project>/src/main/java/com/jless/voxelGame/entity/
```

### Step 3: Migrate Individual Entities
For EntityPenguin and EntityPig, follow the pattern in ENTITY_PORTING_GUIDE.md:

**Key Changes:**
1. Package: `com.jless.voxelGame.entity`
2. Import: `com.jless.voxelGame.worldGen.*`
3. Method signature: `render(Rendering render)`
4. Replace ALL `VoxelRender.drawEntityBoxShader(...)` calls with:
```java
   EntityRender.drawEntityWithTransform(
       transformMatrix,  // Matrix comes FIRST now!
       x0, y0, z0,
       x1, y1, z1,
       colorArray
   );
```

## Critical Bug Fix - Leg Animation

Your old code has this bug:
```java
// WRONG - creates rotated matrix but doesn't use it!
Matrix4f leftLeg0 = withPivotRotationX(entityModel, hipX, hipY, hipZ, angle);

VoxelRender.drawEntityBoxShader(
    lx0, y0, z0,
    lx1, y1, z1,
    legColor,
    entityModel  // ❌ Using base model instead of leftLeg0!
);
```

Fixed version:
```java
// CORRECT - uses the rotated matrix
Matrix4f leftLeg0 = withPivotRotationX(entityModel, hipX, hipY, hipZ, angle);

EntityRender.drawEntityWithTransform(
    leftLeg0,  // ✅ Using the rotated matrix
    lx0, y0, z0,
    lx1, y1, z1,
    legColor
);
```

## Verification Checklist

After migration, verify:

- [ ] Project compiles without errors
- [ ] Entities spawn and move correctly
- [ ] Legs animate when walking
- [ ] Legs stop moving when idle
- [ ] Entities turn to face movement direction
- [ ] No visual glitches (stretching, warping)

## Quick Reference: Method Changes

| Old | New |
|-----|-----|
| `VoxelRender.drawEntityBoxShader(x, y, z, x, y, z, rgb, matrix)` | `EntityRender.drawEntityWithTransform(matrix, x, y, z, x, y, z, rgb)` |
| `render(VoxelRender render)` | `render(Rendering render)` |
| `import com.jless.voxel.*` | `import com.jless.voxelGame.worldGen.*` |

## Need Help?

See ENTITY_PORTING_GUIDE.md for:
- Detailed explanations of all changes
- File-by-file migration instructions
- Common pitfalls and how to avoid them
- Complete code examples for EntityPenguin and EntityPig
- Troubleshooting guide

## Entity Pattern Template

Use this as a template for migrating any entity:
```java
package com.jless.voxelGame.entity;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import com.jless.voxelGame.worldGen.*;

public class EntityMyCreature extends Entity {
    // ... constants and fields ...

    public EntityMyCreature(float x, float y, float z) {
        pos.set(x, y, z);
    }

    @Override
    public void update(World world, float dt) {
        // Physics and AI logic
    }

    @Override
    public void render(Rendering render) {
        Matrix4f entityModel = new Matrix4f()
            .translate(pos.x, pos.y, pos.z)
            .rotateY((float)Math.toRadians(yawDeg))
            .translate(-0.5f, 0.0f, -0.7f)
            .scale(0.7f);

        // Body parts without animation
        EntityRender.drawEntityWithTransform(
            entityModel,
            x0, y0, z0, x1, y1, z1,
            colorArray
        );

        // Animated limbs
        Matrix4f leg = withPivotRotationX(
            entityModel,
            pivotX, pivotY, pivotZ,
            animationAngle
        );

        EntityRender.drawEntityWithTransform(
            leg,  // Use transformed matrix!
            x0, y0, z0, x1, y1, z1,
            colorArray
        );
    }
}
```

---

**Remember:** The transform matrix always comes FIRST in the new API!
