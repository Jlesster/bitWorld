# Voxel Game World Generation Tutorial
## Complete Guide: Trees, Biomes, Caves, and Ores

This tutorial provides a complete roadmap for implementing advanced world generation features including diverse biomes, procedural trees, cave systems, and ore distribution for your voxel game using multi-octave Perlin noise and sophisticated generation algorithms.

---

## Table of Contents

1. [Architecture Overview](#architecture-overview)
2. [Phase 1: Enhanced Noise System](#phase-1-enhanced-noise-system)
3. [Phase 2: Biome System](#phase-2-biome-system)
4. [Phase 3: Tree Generation](#phase-3-tree-generation)
5. [Phase 4: Cave Generation](#phase-4-cave-generation)
6. [Phase 5: Ore Distribution](#phase-5-ore-distribution)
7. [Phase 6: Integration & Optimization](#phase-6-integration--optimization)

---

## Architecture Overview

### System Components

```
WorldGenerator
├── NoiseGenerator (Multi-octave Perlin)
├── BiomeGenerator
│   ├── BiomeType enum
│   ├── BiomeMap
│   └── BiomeBlender
├── TerrainGenerator
│   ├── HeightmapGenerator
│   └── TerrainSculptor
├── StructureGenerator
│   ├── TreeGenerator
│   │   ├── OakTree
│   │   ├── SpruceTree
│   │   └── BirchTree
│   └── CaveGenerator
│       ├── WormCaves
│       └── CavernCaves
└── OreGenerator
    ├── OreVein
    └── OreDistribution
```

### Generation Pipeline

```
1. Generate biome map (temperature + moisture)
2. Generate base heightmap
3. Apply biome-specific terrain modifications
4. Carve caves
5. Place ores based on depth
6. Generate surface features (trees, grass)
7. Final post-processing
```

---

## Phase 1: Enhanced Noise System

### Step 1.1: Multi-Octave Noise Generator

This creates more natural-looking terrain by layering multiple noise frequencies.

```java
package com.jless.voxelGame.worldGen.noise;

import java.util.Random;

public class MultiOctaveNoise {
    
    private SimplexNoise[] octaves;
    private double[] frequencies;
    private double[] amplitudes;
    private int octaveCount;
    
    public MultiOctaveNoise(long seed, int octaveCount) {
        this.octaveCount = octaveCount;
        this.octaves = new SimplexNoise[octaveCount];
        this.frequencies = new double[octaveCount];
        this.amplitudes = new double[octaveCount];
        
        Random rand = new Random(seed);
        
        for (int i = 0; i < octaveCount; i++) {
            octaves[i] = new SimplexNoise(rand.nextLong());
            frequencies[i] = Math.pow(2, i);
            amplitudes[i] = Math.pow(0.5, i);
        }
    }
    
    /**
     * Get noise value with multiple octaves
     * @param x X coordinate
     * @param z Z coordinate
     * @param scale Base scale
     * @param persistence How much each octave contributes (0-1)
     * @return Noise value between -1 and 1
     */
    public double getNoise(double x, double z, double scale, double persistence) {
        double total = 0;
        double maxValue = 0;
        
        for (int i = 0; i < octaveCount; i++) {
            double frequency = frequencies[i] / scale;
            double amplitude = Math.pow(persistence, i);
            
            total += octaves[i].noise(x * frequency, z * frequency) * amplitude;
            maxValue += amplitude;
        }
        
        return total / maxValue;
    }
    
    /**
     * Get noise value in 3D space (for caves)
     */
    public double getNoise3D(double x, double y, double z, double scale, double persistence) {
        double total = 0;
        double maxValue = 0;
        
        for (int i = 0; i < octaveCount; i++) {
            double frequency = frequencies[i] / scale;
            double amplitude = Math.pow(persistence, i);
            
            total += octaves[i].noise(x * frequency, y * frequency, z * frequency) * amplitude;
            maxValue += amplitude;
        }
        
        return total / maxValue;
    }
    
    /**
     * Get noise with custom frequency/amplitude control
     */
    public double getNoiseCustom(double x, double z, 
                                  double baseScale,
                                  double[] customFrequencies,
                                  double[] customAmplitudes) {
        double total = 0;
        double maxValue = 0;
        
        int count = Math.min(octaveCount, Math.min(customFrequencies.length, customAmplitudes.length));
        
        for (int i = 0; i < count; i++) {
            double frequency = customFrequencies[i] / baseScale;
            double amplitude = customAmplitudes[i];
            
            total += octaves[i].noise(x * frequency, z * frequency) * amplitude;
            maxValue += amplitude;
        }
        
        return total / maxValue;
    }
}
```

### Step 1.2: SimplexNoise Implementation

```java
package com.jless.voxelGame.worldGen.noise;

import java.util.Random;

public class SimplexNoise {
    
    private int[] perm;
    private static final int[][] grad3 = {
        {1,1,0},{-1,1,0},{1,-1,0},{-1,-1,0},
        {1,0,1},{-1,0,1},{1,0,-1},{-1,0,-1},
        {0,1,1},{0,-1,1},{0,1,-1},{0,-1,-1}
    };
    
    public SimplexNoise(long seed) {
        Random rand = new Random(seed);
        perm = new int[512];
        
        int[] p = new int[256];
        for (int i = 0; i < 256; i++) {
            p[i] = i;
        }
        
        // Shuffle
        for (int i = 255; i > 0; i--) {
            int j = rand.nextInt(i + 1);
            int temp = p[i];
            p[i] = p[j];
            p[j] = temp;
        }
        
        for (int i = 0; i < 512; i++) {
            perm[i] = p[i & 255];
        }
    }
    
    public double noise(double x, double z) {
        // 2D simplex noise implementation
        final double F2 = 0.5 * (Math.sqrt(3.0) - 1.0);
        final double G2 = (3.0 - Math.sqrt(3.0)) / 6.0;
        
        double s = (x + z) * F2;
        int i = fastFloor(x + s);
        int j = fastFloor(z + s);
        
        double t = (i + j) * G2;
        double X0 = i - t;
        double Y0 = j - t;
        double x0 = x - X0;
        double y0 = z - Y0;
        
        int i1, j1;
        if (x0 > y0) {
            i1 = 1;
            j1 = 0;
        } else {
            i1 = 0;
            j1 = 1;
        }
        
        double x1 = x0 - i1 + G2;
        double y1 = y0 - j1 + G2;
        double x2 = x0 - 1.0 + 2.0 * G2;
        double y2 = y0 - 1.0 + 2.0 * G2;
        
        int ii = i & 255;
        int jj = j & 255;
        int gi0 = perm[ii + perm[jj]] % 12;
        int gi1 = perm[ii + i1 + perm[jj + j1]] % 12;
        int gi2 = perm[ii + 1 + perm[jj + 1]] % 12;
        
        double n0 = 0, n1 = 0, n2 = 0;
        
        double t0 = 0.5 - x0 * x0 - y0 * y0;
        if (t0 >= 0) {
            t0 *= t0;
            n0 = t0 * t0 * dot(grad3[gi0], x0, y0);
        }
        
        double t1 = 0.5 - x1 * x1 - y1 * y1;
        if (t1 >= 0) {
            t1 *= t1;
            n1 = t1 * t1 * dot(grad3[gi1], x1, y1);
        }
        
        double t2 = 0.5 - x2 * x2 - y2 * y2;
        if (t2 >= 0) {
            t2 *= t2;
            n2 = t2 * t2 * dot(grad3[gi2], x2, y2);
        }
        
        return 70.0 * (n0 + n1 + n2);
    }
    
    public double noise(double x, double y, double z) {
        // 3D simplex noise for caves
        final double F3 = 1.0/3.0;
        final double G3 = 1.0/6.0;
        
        double s = (x + y + z) * F3;
        int i = fastFloor(x + s);
        int j = fastFloor(y + s);
        int k = fastFloor(z + s);
        
        double t = (i + j + k) * G3;
        double X0 = i - t;
        double Y0 = j - t;
        double Z0 = k - t;
        double x0 = x - X0;
        double y0 = y - Y0;
        double z0 = z - Z0;
        
        int i1, j1, k1;
        int i2, j2, k2;
        
        if (x0 >= y0) {
            if (y0 >= z0) {
                i1 = 1; j1 = 0; k1 = 0; i2 = 1; j2 = 1; k2 = 0;
            } else if (x0 >= z0) {
                i1 = 1; j1 = 0; k1 = 0; i2 = 1; j2 = 0; k2 = 1;
            } else {
                i1 = 0; j1 = 0; k1 = 1; i2 = 1; j2 = 0; k2 = 1;
            }
        } else {
            if (y0 < z0) {
                i1 = 0; j1 = 0; k1 = 1; i2 = 0; j2 = 1; k2 = 1;
            } else if (x0 < z0) {
                i1 = 0; j1 = 1; k1 = 0; i2 = 0; j2 = 1; k2 = 1;
            } else {
                i1 = 0; j1 = 1; k1 = 0; i2 = 1; j2 = 1; k2 = 0;
            }
        }
        
        double x1 = x0 - i1 + G3;
        double y1 = y0 - j1 + G3;
        double z1 = z0 - k1 + G3;
        double x2 = x0 - i2 + 2.0 * G3;
        double y2 = y0 - j2 + 2.0 * G3;
        double z2 = z0 - k2 + 2.0 * G3;
        double x3 = x0 - 1.0 + 3.0 * G3;
        double y3 = y0 - 1.0 + 3.0 * G3;
        double z3 = z0 - 1.0 + 3.0 * G3;
        
        int ii = i & 255;
        int jj = j & 255;
        int kk = k & 255;
        
        int gi0 = perm[ii + perm[jj + perm[kk]]] % 12;
        int gi1 = perm[ii + i1 + perm[jj + j1 + perm[kk + k1]]] % 12;
        int gi2 = perm[ii + i2 + perm[jj + j2 + perm[kk + k2]]] % 12;
        int gi3 = perm[ii + 1 + perm[jj + 1 + perm[kk + 1]]] % 12;
        
        double n0 = 0, n1 = 0, n2 = 0, n3 = 0;
        
        double t0 = 0.6 - x0*x0 - y0*y0 - z0*z0;
        if (t0 >= 0) {
            t0 *= t0;
            n0 = t0 * t0 * dot(grad3[gi0], x0, y0, z0);
        }
        
        double t1 = 0.6 - x1*x1 - y1*y1 - z1*z1;
        if (t1 >= 0) {
            t1 *= t1;
            n1 = t1 * t1 * dot(grad3[gi1], x1, y1, z1);
        }
        
        double t2 = 0.6 - x2*x2 - y2*y2 - z2*z2;
        if (t2 >= 0) {
            t2 *= t2;
            n2 = t2 * t2 * dot(grad3[gi2], x2, y2, z2);
        }
        
        double t3 = 0.6 - x3*x3 - y3*y3 - z3*z3;
        if (t3 >= 0) {
            t3 *= t3;
            n3 = t3 * t3 * dot(grad3[gi3], x3, y3, z3);
        }
        
        return 32.0 * (n0 + n1 + n2 + n3);
    }
    
    private double dot(int[] g, double x, double y) {
        return g[0] * x + g[1] * y;
    }
    
    private double dot(int[] g, double x, double y, double z) {
        return g[0] * x + g[1] * y + g[2] * z;
    }
    
    private int fastFloor(double x) {
        int xi = (int) x;
        return x < xi ? xi - 1 : xi;
    }
}
```

---

## Phase 2: Biome System

### Step 2.1: Define Biome Types

```java
package com.jless.voxelGame.worldGen.biomes;

import com.jless.voxelGame.blocks.*;

public enum BiomeType {
    
    OCEAN(
        BlockID.SAND,      // Surface
        BlockID.SAND,      // Subsurface
        BlockID.STONE,     // Stone
        55,                // Base height
        5,                 // Height variation
        0.5,               // Temperature
        0.8,               // Moisture
        new float[] {0.2f, 0.3f, 0.8f}  // Sky color
    ),
    
    PLAINS(
        BlockID.GRASS,
        BlockID.DIRT,
        BlockID.STONE,
        65,
        8,
        0.6,
        0.4,
        new float[] {0.6f, 0.8f, 1.0f}
    ),
    
    FOREST(
        BlockID.GRASS,
        BlockID.DIRT,
        BlockID.STONE,
        67,
        12,
        0.7,
        0.7,
        new float[] {0.5f, 0.7f, 0.9f}
    ),
    
    DESERT(
        BlockID.SAND,
        BlockID.SAND,
        BlockID.STONE,
        64,
        15,
        0.9,
        0.1,
        new float[] {0.9f, 0.8f, 0.6f}
    ),
    
    MOUNTAINS(
        BlockID.STONE,
        BlockID.STONE,
        BlockID.STONE,
        80,
        40,
        0.3,
        0.3,
        new float[] {0.7f, 0.8f, 1.0f}
    ),
    
    TAIGA(
        BlockID.GRASS,
        BlockID.DIRT,
        BlockID.STONE,
        66,
        10,
        0.2,
        0.5,
        new float[] {0.6f, 0.7f, 0.9f}
    ),
    
    SWAMP(
        BlockID.GRASS,
        BlockID.DIRT,
        BlockID.STONE,
        62,
        4,
        0.8,
        0.9,
        new float[] {0.5f, 0.6f, 0.7f}
    ),
    
    SNOWY_TUNDRA(
        BlockID.GRASS,  // Will be snow-covered
        BlockID.DIRT,
        BlockID.STONE,
        63,
        6,
        0.0,
        0.5,
        new float[] {0.8f, 0.9f, 1.0f}
    );
    
    public final byte surfaceBlock;
    public final byte subsurfaceBlock;
    public final byte stoneBlock;
    public final int baseHeight;
    public final int heightVariation;
    public final double temperature;
    public final double moisture;
    public final float[] skyColor;
    
    BiomeType(byte surface, byte subsurface, byte stone, 
              int baseHeight, int heightVar,
              double temp, double moisture,
              float[] skyColor) {
        this.surfaceBlock = surface;
        this.subsurfaceBlock = subsurface;
        this.stoneBlock = stone;
        this.baseHeight = baseHeight;
        this.heightVariation = heightVar;
        this.temperature = temp;
        this.moisture = moisture;
        this.skyColor = skyColor;
    }
    
    /**
     * Get tree density for this biome (0-1)
     */
    public double getTreeDensity() {
        switch (this) {
            case FOREST: return 0.15;
            case TAIGA: return 0.12;
            case PLAINS: return 0.02;
            case SWAMP: return 0.08;
            default: return 0.0;
        }
    }
    
    /**
     * Get primary tree type for this biome
     */
    public TreeType getPrimaryTree() {
        switch (this) {
            case FOREST: return TreeType.OAK;
            case TAIGA: return TreeType.SPRUCE;
            case SWAMP: return TreeType.OAK;
            default: return TreeType.OAK;
        }
    }
}
```

### Step 2.2: Biome Generator

```java
package com.jless.voxelGame.worldGen.biomes;

import com.jless.voxelGame.worldGen.noise.*;

public class BiomeGenerator {
    
    private MultiOctaveNoise temperatureNoise;
    private MultiOctaveNoise moistureNoise;
    private MultiOctaveNoise biomeVariationNoise;
    
    private static final double TEMPERATURE_SCALE = 800.0;
    private static final double MOISTURE_SCALE = 600.0;
    private static final double VARIATION_SCALE = 400.0;
    
    public BiomeGenerator(long seed) {
        this.temperatureNoise = new MultiOctaveNoise(seed, 4);
        this.moistureNoise = new MultiOctaveNoise(seed + 1000, 4);
        this.biomeVariationNoise = new MultiOctaveNoise(seed + 2000, 3);
    }
    
    /**
     * Get biome at world coordinates
     */
    public BiomeType getBiome(int x, int z) {
        double temp = getTemperature(x, z);
        double moisture = getMoisture(x, z);
        
        return selectBiome(temp, moisture);
    }
    
    /**
     * Get temperature at coordinates (0-1, cold to hot)
     */
    public double getTemperature(int x, int z) {
        double noise = temperatureNoise.getNoise(x, z, TEMPERATURE_SCALE, 0.5);
        // Convert from -1,1 to 0,1
        return (noise + 1.0) * 0.5;
    }
    
    /**
     * Get moisture at coordinates (0-1, dry to wet)
     */
    public double getMoisture(int x, int z) {
        double noise = moistureNoise.getNoise(x, z, MOISTURE_SCALE, 0.5);
        return (noise + 1.0) * 0.5;
    }
    
    /**
     * Get biome variation (for mixing biomes)
     */
    public double getBiomeVariation(int x, int z) {
        return biomeVariationNoise.getNoise(x, z, VARIATION_SCALE, 0.6);
    }
    
    /**
     * Select biome based on temperature and moisture
     * 
     * Biome distribution map:
     *           Dry (0)    Medium     Wet (1)
     * Cold (0)  Tundra     Taiga      Taiga
     * Medium    Desert     Plains     Forest
     * Hot (1)   Desert     Plains     Swamp
     * 
     * Mountains override based on height
     * Ocean for low areas
     */
    private BiomeType selectBiome(double temp, double moisture) {
        // Cold biomes
        if (temp < 0.25) {
            if (moisture < 0.33) {
                return BiomeType.SNOWY_TUNDRA;
            } else {
                return BiomeType.TAIGA;
            }
        }
        // Temperate biomes
        else if (temp < 0.75) {
            if (moisture < 0.33) {
                return BiomeType.DESERT;
            } else if (moisture < 0.66) {
                return BiomeType.PLAINS;
            } else {
                return BiomeType.FOREST;
            }
        }
        // Hot biomes
        else {
            if (moisture < 0.5) {
                return BiomeType.DESERT;
            } else if (moisture < 0.75) {
                return BiomeType.PLAINS;
            } else {
                return BiomeType.SWAMP;
            }
        }
    }
    
    /**
     * Get blended height for smooth biome transitions
     */
    public int getBlendedHeight(int x, int z, int baseHeight) {
        // Sample surrounding biomes for smooth transitions
        final int sampleRadius = 8;
        double totalHeight = 0;
        double totalWeight = 0;
        
        for (int dx = -sampleRadius; dx <= sampleRadius; dx += sampleRadius) {
            for (int dz = -sampleRadius; dz <= sampleRadius; dz += sampleRadius) {
                BiomeType biome = getBiome(x + dx, z + dz);
                double distance = Math.sqrt(dx * dx + dz * dz);
                double weight = 1.0 / (1.0 + distance);
                
                totalHeight += biome.baseHeight * weight;
                totalWeight += weight;
            }
        }
        
        return (int) (totalHeight / totalWeight);
    }
}
```

### Step 2.3: Biome-Aware Terrain Generator

```java
package com.jless.voxelGame.worldGen.terrain;

import com.jless.voxelGame.worldGen.biomes.*;
import com.jless.voxelGame.worldGen.noise.*;

public class TerrainGenerator {
    
    private BiomeGenerator biomeGen;
    private MultiOctaveNoise continentNoise;
    private MultiOctaveNoise terrainNoise;
    private MultiOctaveNoise detailNoise;
    
    private static final double CONTINENT_SCALE = 2000.0;
    private static final double TERRAIN_SCALE = 300.0;
    private static final double DETAIL_SCALE = 80.0;
    
    public TerrainGenerator(long seed, BiomeGenerator biomeGen) {
        this.biomeGen = biomeGen;
        this.continentNoise = new MultiOctaveNoise(seed, 4);
        this.terrainNoise = new MultiOctaveNoise(seed + 100, 5);
        this.detailNoise = new MultiOctaveNoise(seed + 200, 3);
    }
    
    /**
     * Generate height at world coordinates
     */
    public int getHeight(int x, int z) {
        BiomeType biome = biomeGen.getBiome(x, z);
        
        // Continental scale (determines if land or ocean)
        double continent = continentNoise.getNoise(x, z, CONTINENT_SCALE, 0.5);
        
        // Base terrain
        double terrain = terrainNoise.getNoise(x, z, TERRAIN_SCALE, 0.55);
        
        // Fine details
        double detail = detailNoise.getNoise(x, z, DETAIL_SCALE, 0.6);
        
        // Start with biome base height
        double height = biome.baseHeight;
        
        // Add continental variation
        height += continent * 15;
        
        // Add terrain variation scaled by biome
        height += terrain * biome.heightVariation;
        
        // Add detail
        height += detail * 4;
        
        // Special cases
        if (biome == BiomeType.MOUNTAINS) {
            // Extra height for mountains
            double mountainNoise = terrainNoise.getNoise(x, z, 200.0, 0.65);
            height += Math.max(0, mountainNoise * 30);
        }
        
        if (height < 60) {
            // Below sea level = ocean
            return (int) height;
        }
        
        return (int) height;
    }
    
    /**
     * Get surface depth (how many blocks of surface material)
     */
    public int getSurfaceDepth(int x, int z) {
        BiomeType biome = biomeGen.getBiome(x, z);
        
        // Desert has deeper sand
        if (biome == BiomeType.DESERT) {
            return 4 + (int)(detailNoise.getNoise(x, z, 50.0, 0.5) * 2);
        }
        
        return 1;
    }
    
    /**
     * Get subsurface depth (dirt layer)
     */
    public int getSubsurfaceDepth(int x, int z) {
        return 3 + (int)(detailNoise.getNoise(x + 1000, z + 1000, 40.0, 0.5) * 2);
    }
}
```

---

## Phase 3: Tree Generation

### Step 3.1: Tree Type System

```java
package com.jless.voxelGame.worldGen.structures;

public enum TreeType {
    OAK,
    SPRUCE,
    BIRCH
}
```

### Step 3.2: Base Tree Generator

```java
package com.jless.voxelGame.worldGen.structures;

import com.jless.voxelGame.blocks.*;
import com.jless.voxelGame.worldGen.*;
import java.util.Random;

public abstract class TreeGenerator {
    
    protected Random random;
    
    public TreeGenerator(long seed) {
        this.random = new Random(seed);
    }
    
    /**
     * Check if tree can be placed at location
     */
    protected boolean canPlaceTree(World world, int x, int y, int z, int height) {
        if (y < 1 || y + height > 250) {
            return false;
        }
        
        // Check if there's solid ground
        byte groundBlock = world.getIfLoaded(x, y - 1, z);
        if (groundBlock != BlockID.GRASS && 
            groundBlock != BlockID.DIRT) {
            return false;
        }
        
        // Check if space is clear
        for (int dy = 0; dy < height; dy++) {
            byte block = world.getIfLoaded(x, y + dy, z);
            if (block != BlockID.AIR) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Place a single log block
     */
    protected void placeLog(World world, int x, int y, int z, byte logType) {
        world.set(x, y, z, logType);
    }
    
    /**
     * Place a single leaf block
     */
    protected void placeLeaves(World world, int x, int y, int z, byte leafType) {
        byte existing = world.getIfLoaded(x, y, z);
        if (existing == BlockID.AIR) {
            world.set(x, y, z, leafType);
        }
    }
    
    /**
     * Generate tree at location
     */
    public abstract void generate(World world, int x, int y, int z);
}
```

### Step 3.3: Oak Tree Generator

```java
package com.jless.voxelGame.worldGen.structures;

import com.jless.voxelGame.blocks.*;
import com.jless.voxelGame.worldGen.*;

public class OakTreeGenerator extends TreeGenerator {
    
    public OakTreeGenerator(long seed) {
        super(seed);
    }
    
    @Override
    public void generate(World world, int x, int y, int z) {
        int trunkHeight = 4 + random.nextInt(3); // 4-6 blocks
        
        if (!canPlaceTree(world, x, y, z, trunkHeight + 4)) {
            return;
        }
        
        // Place trunk
        for (int dy = 0; dy < trunkHeight; dy++) {
            placeLog(world, x, y + dy, z, BlockID.OAK_LOG);
        }
        
        // Place leaves (rounded canopy)
        int leafStart = trunkHeight - 2;
        int leafHeight = 4;
        
        for (int dy = 0; dy < leafHeight; dy++) {
            int currentY = y + leafStart + dy;
            int radius = getLeafRadius(dy, leafHeight);
            
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    // Create rounded shape
                    int distSq = dx * dx + dz * dz;
                    if (distSq <= radius * radius) {
                        // Random sparse leaves at edges
                        if (distSq == radius * radius && random.nextFloat() < 0.3f) {
                            continue;
                        }
                        
                        placeLeaves(world, x + dx, currentY, z + dz, BlockID.OAK_LEAVES);
                    }
                }
            }
        }
    }
    
    private int getLeafRadius(int layer, int totalLayers) {
        if (layer == 0) return 2;
        if (layer == totalLayers - 1) return 1;
        return 2;
    }
}
```

### Step 3.4: Spruce Tree Generator

```java
package com.jless.voxelGame.worldGen.structures;

import com.jless.voxelGame.blocks.*;
import com.jless.voxelGame.worldGen.*;

public class SpruceTreeGenerator extends TreeGenerator {
    
    public SpruceTreeGenerator(long seed) {
        super(seed);
    }
    
    @Override
    public void generate(World world, int x, int y, int z) {
        int trunkHeight = 6 + random.nextInt(4); // 6-9 blocks
        
        if (!canPlaceTree(world, x, y, z, trunkHeight + 3)) {
            return;
        }
        
        // Place trunk
        for (int dy = 0; dy < trunkHeight; dy++) {
            placeLog(world, x, y + dy, z, BlockID.SPRUCE_LOG);
        }
        
        // Conical shape - narrow at top, wider at bottom
        int leafStart = 2;
        
        for (int dy = leafStart; dy < trunkHeight; dy++) {
            int currentY = y + dy;
            int layerFromTop = trunkHeight - dy;
            
            // Radius increases as we go down
            int radius = Math.min(2, (layerFromTop + 1) / 2);
            
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    // Square pyramid shape
                    int dist = Math.max(Math.abs(dx), Math.abs(dz));
                    
                    if (dist <= radius) {
                        // Skip center where trunk is
                        if (dx == 0 && dz == 0) continue;
                        
                        placeLeaves(world, x + dx, currentY, z + dz, BlockID.OAK_LEAVES);
                    }
                }
            }
        }
        
        // Top point
        placeLeaves(world, x, y + trunkHeight, z, BlockID.OAK_LEAVES);
    }
}
```

### Step 3.5: Tree Placement System

```java
package com.jless.voxelGame.worldGen.structures;

import com.jless.voxelGame.worldGen.*;
import com.jless.voxelGame.worldGen.biomes.*;
import java.util.Random;

public class TreePlacer {
    
    private OakTreeGenerator oakGen;
    private SpruceTreeGenerator spruceGen;
    private BiomeGenerator biomeGen;
    private Random random;
    
    public TreePlacer(long seed, BiomeGenerator biomeGen) {
        this.biomeGen = biomeGen;
        this.oakGen = new OakTreeGenerator(seed);
        this.spruceGen = new SpruceTreeGenerator(seed + 500);
        this.random = new Random(seed);
    }
    
    /**
     * Attempt to place trees in a chunk
     */
    public void placeTrees(World world, int chunkX, int chunkZ) {
        // Use chunk position to seed random
        long chunkSeed = (long)chunkX * 341873128712L + (long)chunkZ * 132897987541L;
        random.setSeed(chunkSeed);
        
        int worldX = chunkX * 16;
        int worldZ = chunkZ * 16;
        
        // Try to place several trees per chunk
        int attempts = 8;
        
        for (int i = 0; i < attempts; i++) {
            int dx = random.nextInt(16);
            int dz = random.nextInt(16);
            int x = worldX + dx;
            int z = worldZ + dz;
            
            BiomeType biome = biomeGen.getBiome(x, z);
            double treeDensity = biome.getTreeDensity();
            
            if (random.nextDouble() < treeDensity) {
                // Find surface height
                int y = world.getSurfY(x, z);
                
                if (y > 0 && y < 240) {
                    TreeType treeType = biome.getPrimaryTree();
                    placeTree(world, x, y + 1, z, treeType);
                }
            }
        }
    }
    
    private void placeTree(World world, int x, int y, int z, TreeType type) {
        switch (type) {
            case OAK:
                oakGen.generate(world, x, y, z);
                break;
            case SPRUCE:
                spruceGen.generate(world, x, y, z);
                break;
        }
    }
}
```

---

## Phase 4: Cave Generation

### Step 4.1: 3D Worm Cave Generator

```java
package com.jless.voxelGame.worldGen.caves;

import com.jless.voxelGame.blocks.*;
import com.jless.voxelGame.worldGen.*;
import com.jless.voxelGame.worldGen.noise.*;
import java.util.Random;

public class WormCaveGenerator {
    
    private MultiOctaveNoise caveNoise;
    private Random random;
    
    private static final double CAVE_THRESHOLD = 0.6;
    private static final double CAVE_SCALE = 40.0;
    
    public WormCaveGenerator(long seed) {
        this.caveNoise = new MultiOctaveNoise(seed, 4);
        this.random = new Random(seed);
    }
    
    /**
     * Carve worm-style caves using 3D noise
     */
    public void carveCaves(World world, int chunkX, int chunkZ) {
        int worldX = chunkX * 16;
        int worldZ = chunkZ * 16;
        
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int wx = worldX + x;
                int wz = worldZ + z;
                
                // Only carve caves below surface
                int surfaceY = world.getSurfY(wx, wz);
                int minY = 5;  // Don't go below bedrock
                int maxY = surfaceY - 5;  // Stay away from surface
                
                for (int y = minY; y < maxY; y++) {
                    double noise = caveNoise.getNoise3D(wx, y, wz, CAVE_SCALE, 0.6);
                    
                    // Additional vertical bias - fewer caves near top/bottom
                    double verticalBias = 1.0 - Math.abs((y - 64.0) / 64.0);
                    noise *= verticalBias;
                    
                    if (noise > CAVE_THRESHOLD) {
                        byte currentBlock = world.getIfLoaded(wx, y, wz);
                        if (Blocks.SOLID[currentBlock]) {
                            world.set(wx, y, wz, BlockID.AIR);
                        }
                    }
                }
            }
        }
    }
}
```

### Step 4.2: Cavern Generator

```java
package com.jless.voxelGame.worldGen.caves;

import com.jless.voxelGame.blocks.*;
import com.jless.voxelGame.worldGen.*;
import com.jless.voxelGame.worldGen.noise.*;
import java.util.Random;

public class CavernGenerator {
    
    private MultiOctaveNoise cavernNoise;
    private MultiOctaveNoise heightNoise;
    private Random random;
    
    private static final double CAVERN_SCALE = 80.0;
    private static final double CAVERN_THRESHOLD = 0.65;
    
    public CavernGenerator(long seed) {
        this.cavernNoise = new MultiOctaveNoise(seed + 1000, 3);
        this.heightNoise = new MultiOctaveNoise(seed + 2000, 2);
        this.random = new Random(seed);
    }
    
    /**
     * Generate large open caverns
     */
    public void generateCaverns(World world, int chunkX, int chunkZ) {
        int worldX = chunkX * 16;
        int worldZ = chunkZ * 16;
        
        // Check if this chunk should have a cavern
        long chunkSeed = (long)chunkX * 341873128712L + (long)chunkZ * 132897987541L;
        random.setSeed(chunkSeed);
        
        if (random.nextDouble() > 0.05) {
            return; // Only 5% of chunks have caverns
        }
        
        // Determine cavern center height
        int cavernY = 20 + random.nextInt(40); // Between y=20 and y=60
        int cavernHeight = 15 + random.nextInt(15); // 15-30 blocks tall
        
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int wx = worldX + x;
                int wz = worldZ + z;
                
                // Get cavern strength at this XZ position
                double cavernStrength = cavernNoise.getNoise(wx, wz, CAVERN_SCALE, 0.5);
                
                if (cavernStrength > CAVERN_THRESHOLD) {
                    // Vary the height
                    double heightVariation = heightNoise.getNoise(wx, wz, 30.0, 0.5);
                    int localHeight = cavernHeight + (int)(heightVariation * 5);
                    
                    int minY = cavernY - localHeight / 2;
                    int maxY = cavernY + localHeight / 2;
                    
                    for (int y = minY; y < maxY; y++) {
                        if (y >= 5 && y < 250) {
                            byte currentBlock = world.getIfLoaded(wx, y, wz);
                            if (Blocks.SOLID[currentBlock]) {
                                world.set(wx, y, wz, BlockID.AIR);
                            }
                        }
                    }
                }
            }
        }
    }
}
```

### Step 4.3: Cave System Manager

```java
package com.jless.voxelGame.worldGen.caves;

import com.jless.voxelGame.worldGen.*;

public class CaveSystem {
    
    private WormCaveGenerator wormCaves;
    private CavernGenerator caverns;
    
    public CaveSystem(long seed) {
        this.wormCaves = new WormCaveGenerator(seed);
        this.caverns = new CavernGenerator(seed);
    }
    
    /**
     * Generate all cave types for a chunk
     */
    public void generateCaves(World world, int chunkX, int chunkZ) {
        // Generate worm caves (common)
        wormCaves.carveCaves(world, chunkX, chunkZ);
        
        // Generate caverns (rare)
        caverns.generateCaverns(world, chunkX, chunkZ);
    }
}
```

---

## Phase 5: Ore Distribution

### Step 5.1: Ore Configuration

```java
package com.jless.voxelGame.worldGen.ores;

import com.jless.voxelGame.blocks.*;

public class OreConfig {
    
    public final byte oreBlock;
    public final int minHeight;
    public final int maxHeight;
    public final int veinSize;
    public final int veinsPerChunk;
    public final double rarity; // 0-1, chance of vein spawning
    
    public OreConfig(byte oreBlock, int minHeight, int maxHeight, 
                     int veinSize, int veinsPerChunk, double rarity) {
        this.oreBlock = oreBlock;
        this.minHeight = minHeight;
        this.maxHeight = maxHeight;
        this.veinSize = veinSize;
        this.veinsPerChunk = veinsPerChunk;
        this.rarity = rarity;
    }
    
    // Predefined ore configurations
    public static final OreConfig COAL = new OreConfig(
        BlockID.COAL_ORE, 5, 128, 8, 20, 1.0
    );
    
    public static final OreConfig IRON = new OreConfig(
        BlockID.IRON_ORE, 5, 64, 6, 15, 0.9
    );
    
    public static final OreConfig COPPER = new OreConfig(
        BlockID.COPPER_ORE, 5, 96, 7, 12, 0.85
    );
    
    public static final OreConfig DIAMOND = new OreConfig(
        BlockID.DIAMOND_ORE, 5, 16, 4, 1, 0.7
    );
    
    public static OreConfig[] getAllOres() {
        return new OreConfig[] {
            COAL, IRON, COPPER, DIAMOND
        };
    }
}
```

### Step 5.2: Ore Vein Generator

```java
package com.jless.voxelGame.worldGen.ores;

import com.jless.voxelGame.blocks.*;
import com.jless.voxelGame.worldGen.*;
import java.util.Random;

public class OreVeinGenerator {
    
    private Random random;
    
    public OreVeinGenerator(long seed) {
        this.random = new Random(seed);
    }
    
    /**
     * Generate a single ore vein
     */
    public void generateVein(World world, int startX, int startY, int startZ, 
                            OreConfig config) {
        if (startY < config.minHeight || startY > config.maxHeight) {
            return;
        }
        
        // Random spherical vein
        double angle = random.nextDouble() * Math.PI;
        double horizontalAngle = random.nextDouble() * Math.PI * 2;
        
        int blocksPlaced = 0;
        int maxBlocks = config.veinSize;
        
        double x = startX;
        double y = startY;
        double z = startZ;
        
        for (int i = 0; i < maxBlocks; i++) {
            // Place ore at current position
            int ix = (int) Math.round(x);
            int iy = (int) Math.round(y);
            int iz = (int) Math.round(z);
            
            if (canPlaceOre(world, ix, iy, iz, config)) {
                world.set(ix, iy, iz, config.oreBlock);
                blocksPlaced++;
            }
            
            // Move in random direction with some clustering
            x += Math.sin(angle) * Math.cos(horizontalAngle) * 0.7;
            y += Math.cos(angle) * 0.7;
            z += Math.sin(angle) * Math.sin(horizontalAngle) * 0.7;
            
            // Slightly vary direction
            angle += (random.nextDouble() - 0.5) * 0.3;
            horizontalAngle += (random.nextDouble() - 0.5) * 0.3;
            
            // Stop early if we've wandered too far
            double dist = Math.sqrt(
                (x - startX) * (x - startX) +
                (y - startY) * (y - startY) +
                (z - startZ) * (z - startZ)
            );
            
            if (dist > maxBlocks * 0.8) {
                break;
            }
        }
    }
    
    private boolean canPlaceOre(World world, int x, int y, int z, OreConfig config) {
        if (y < config.minHeight || y > config.maxHeight) {
            return false;
        }
        
        byte block = world.getIfLoaded(x, y, z);
        
        // Can only replace stone
        return block == BlockID.STONE;
    }
}
```

### Step 5.3: Ore Distribution Manager

```java
package com.jless.voxelGame.worldGen.ores;

import com.jless.voxelGame.worldGen.*;
import java.util.Random;

public class OreDistributor {
    
    private OreVeinGenerator veinGen;
    private OreConfig[] oreConfigs;
    private Random random;
    
    public OreDistributor(long seed) {
        this.veinGen = new OreVeinGenerator(seed);
        this.oreConfigs = OreConfig.getAllOres();
        this.random = new Random(seed);
    }
    
    /**
     * Distribute ores throughout a chunk
     */
    public void distributeOres(World world, int chunkX, int chunkZ) {
        long chunkSeed = (long)chunkX * 341873128712L + (long)chunkZ * 132897987541L;
        random.setSeed(chunkSeed);
        
        int worldX = chunkX * 16;
        int worldZ = chunkZ * 16;
        
        for (OreConfig config : oreConfigs) {
            // Number of veins to attempt
            int attempts = config.veinsPerChunk;
            
            for (int i = 0; i < attempts; i++) {
                // Check rarity
                if (random.nextDouble() > config.rarity) {
                    continue;
                }
                
                // Random position in chunk
                int x = worldX + random.nextInt(16);
                int z = worldZ + random.nextInt(16);
                int y = config.minHeight + random.nextInt(
                    config.maxHeight - config.minHeight + 1
                );
                
                veinGen.generateVein(world, x, y, z, config);
            }
        }
    }
}
```

---

## Phase 6: Integration & Optimization

### Step 6.1: Master World Generator

```java
package com.jless.voxelGame.worldGen;

import com.jless.voxelGame.blocks.*;
import com.jless.voxelGame.worldGen.biomes.*;
import com.jless.voxelGame.worldGen.terrain.*;
import com.jless.voxelGame.worldGen.structures.*;
import com.jless.voxelGame.worldGen.caves.*;
import com.jless.voxelGame.worldGen.ores.*;

public class EnhancedWorldGenerator {
    
    private long seed;
    
    private BiomeGenerator biomeGen;
    private TerrainGenerator terrainGen;
    private TreePlacer treePlacer;
    private CaveSystem caveSystem;
    private OreDistributor oreDistributor;
    
    public EnhancedWorldGenerator(long seed) {
        this.seed = seed;
        
        this.biomeGen = new BiomeGenerator(seed);
        this.terrainGen = new TerrainGenerator(seed, biomeGen);
        this.treePlacer = new TreePlacer(seed, biomeGen);
        this.caveSystem = new CaveSystem(seed);
        this.oreDistributor = new OreDistributor(seed);
    }
    
    /**
     * Generate a complete chunk
     */
    public void generateChunk(Chunk chunk, int chunkX, int chunkZ) {
        int worldX = chunkX * 16;
        int worldZ = chunkZ * 16;
        
        // Phase 1: Generate base terrain
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int wx = worldX + x;
                int wz = worldZ + z;
                
                generateColumn(chunk, x, z, wx, wz);
            }
        }
        
        chunk.uploaded = false;
        chunk.needsRebuild = true;
    }
    
    /**
     * Generate a single vertical column
     */
    private void generateColumn(Chunk chunk, int localX, int localZ, int worldX, int worldZ) {
        BiomeType biome = biomeGen.getBiome(worldX, worldZ);
        int height = terrainGen.getHeight(worldX, worldZ);
        
        int surfaceDepth = terrainGen.getSurfaceDepth(worldX, worldZ);
        int subsurfaceDepth = terrainGen.getSubsurfaceDepth(worldX, worldZ);
        
        for (int y = 0; y < 256; y++) {
            byte block = BlockID.AIR;
            
            if (y == 0) {
                block = BlockID.BEDROCK;
            } else if (y < height - surfaceDepth - subsurfaceDepth) {
                block = biome.stoneBlock;
            } else if (y < height - surfaceDepth) {
                block = biome.subsurfaceBlock;
            } else if (y < height) {
                block = biome.surfaceBlock;
            } else if (y < 60) {
                // Water for ocean
                // block = BlockID.WATER; // If you have water
            }
            
            chunk.setLocal(localX, y, localZ, block);
        }
    }
    
    /**
     * Post-process chunk (caves, ores, trees)
     * Call this after neighboring chunks are generated
     */
    public void postProcessChunk(World world, int chunkX, int chunkZ) {
        // Carve caves
        caveSystem.generateCaves(world, chunkX, chunkZ);
        
        // Distribute ores
        oreDistributor.distributeOres(world, chunkX, chunkZ);
        
        // Place trees
        treePlacer.placeTrees(world, chunkX, chunkZ);
        
        // Mark chunk for rebuild
        Chunk chunk = world.getChunkIfLoaded(chunkX, chunkZ);
        if (chunk != null) {
            chunk.needsRebuild = true;
        }
    }
}
```

### Step 6.2: Update World Class

```java
// In your World.java, update the generation method:

public void generateChunk(int chunkX, int chunkZ) {
    Chunk chunk = new Chunk(chunkX, chunkZ);
    
    // Generate base terrain
    enhancedWorldGen.generateChunk(chunk, chunkX, chunkZ);
    
    chunks.put(chunkKey(chunkX, chunkZ), chunk);
    
    // Post-process after chunk is in world
    enhancedWorldGen.postProcessChunk(this, chunkX, chunkZ);
}
```

### Step 6.3: Chunk Generation Pipeline

```java
package com.jless.voxelGame.worldGen;

import java.util.*;
import java.util.concurrent.*;

public class ChunkGenerationPipeline {
    
    private EnhancedWorldGenerator worldGen;
    private World world;
    
    private Set<Long> generatedChunks;
    private Queue<ChunkCoord> postProcessQueue;
    
    private static class ChunkCoord {
        int x, z;
        ChunkCoord(int x, int z) {
            this.x = x;
            this.z = z;
        }
    }
    
    public ChunkGenerationPipeline(long seed, World world) {
        this.worldGen = new EnhancedWorldGenerator(seed);
        this.world = world;
        this.generatedChunks = new HashSet<>();
        this.postProcessQueue = new LinkedList<>();
    }
    
    /**
     * Generate chunk with proper pipeline
     */
    public void generateChunk(int chunkX, int chunkZ) {
        long key = chunkKey(chunkX, chunkZ);
        
        if (generatedChunks.contains(key)) {
            return;
        }
        
        // Generate base chunk
        Chunk chunk = new Chunk(chunkX, chunkZ);
        worldGen.generateChunk(chunk, chunkX, chunkZ);
        world.addChunk(chunk);
        
        generatedChunks.add(key);
        
        // Queue for post-processing
        postProcessQueue.add(new ChunkCoord(chunkX, chunkZ));
        
        // Process queue
        processPostProcessQueue();
    }
    
    /**
     * Process chunks that are ready for post-processing
     * (have all neighbors generated)
     */
    private void processPostProcessQueue() {
        Iterator<ChunkCoord> iter = postProcessQueue.iterator();
        
        while (iter.hasNext()) {
            ChunkCoord coord = iter.next();
            
            // Check if all neighbors exist
            if (hasAllNeighbors(coord.x, coord.z)) {
                worldGen.postProcessChunk(world, coord.x, coord.z);
                iter.remove();
            }
        }
    }
    
    private boolean hasAllNeighbors(int chunkX, int chunkZ) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                long key = chunkKey(chunkX + dx, chunkZ + dz);
                if (!generatedChunks.contains(key)) {
                    return false;
                }
            }
        }
        return true;
    }
    
    private long chunkKey(int x, int z) {
        return ((long)x << 32) | (z & 0xFFFFFFFFL);
    }
}
```

---

## Implementation Roadmap

### Week 1: Noise Foundation
1. ✅ Implement SimplexNoise
2. ✅ Implement MultiOctaveNoise
3. ✅ Test noise visualization
4. ✅ Tune noise parameters

### Week 2: Biome System
1. ✅ Define BiomeType enum
2. ✅ Implement BiomeGenerator
3. ✅ Create TerrainGenerator
4. ✅ Test biome distribution
5. ✅ Tune biome transitions

### Week 3: Trees
1. ✅ Create TreeGenerator base
2. ✅ Implement OakTreeGenerator
3. ✅ Implement SpruceTreeGenerator
4. ✅ Create TreePlacer
5. ✅ Test tree placement

### Week 4: Caves
1. ✅ Implement WormCaveGenerator
2. ✅ Implement CavernGenerator
3. ✅ Create CaveSystem
4. ✅ Test cave generation
5. ✅ Tune cave density

### Week 5: Ores
1. ✅ Create OreConfig
2. ✅ Implement OreVeinGenerator
3. ✅ Create OreDistributor
4. ✅ Test ore distribution
5. ✅ Balance ore spawning

### Week 6: Integration
1. ✅ Create EnhancedWorldGenerator
2. ✅ Implement ChunkGenerationPipeline
3. ✅ Update World class
4. ✅ Test complete system
5. ✅ Performance optimization

---

## Testing Checklist

### Noise System
- [ ] Noise functions return values in expected range
- [ ] Multiple octaves produce natural variation
- [ ] Seeded noise is deterministic
- [ ] 3D noise works correctly for caves

### Biomes
- [ ] All biome types generate
- [ ] Biome transitions are smooth
- [ ] Temperature/moisture maps make sense
- [ ] Biome-specific blocks placed correctly
- [ ] Mountain biomes are tall
- [ ] Ocean biomes are low

### Trees
- [ ] Trees generate in correct biomes
- [ ] Tree density feels right
- [ ] Trees don't overlap structures
- [ ] Different tree types visible
- [ ] Leaves form proper canopy
- [ ] No floating trees

### Caves
- [ ] Worm caves are interconnected
- [ ] Caverns are large and open
- [ ] Caves don't breach surface too often
- [ ] Cave density is balanced
- [ ] Caves expose ores

### Ores
- [ ] All ore types generate
- [ ] Ore veins are appropriate size
- [ ] Ores only in correct height ranges
- [ ] Diamond is rare
- [ ] Coal is common
- [ ] Ores only replace stone

### Performance
- [ ] Chunk generation is fast enough
- [ ] No visible lag spikes
- [ ] Memory usage is reasonable
- [ ] Threading works correctly

---

## Optimization Tips

### 1. Noise Caching
```java
// Cache noise values for reuse
private Map<Long, Double> noiseCache = new HashMap<>();

public double getCachedNoise(int x, int z) {
    long key = ((long)x << 32) | (z & 0xFFFFFFFFL);
    return noiseCache.computeIfAbsent(key, k -> computeNoise(x, z));
}
```

### 2. Chunk Border Handling
```java
// Generate extra border for trees/structures
public void generateChunkWithBorder(int chunkX, int chunkZ) {
    // Generate 18x18 instead of 16x16
    // Trees can extend into neighboring chunks
}
```

### 3. Batch Processing
```java
// Process multiple chunks in parallel
ExecutorService executor = Executors.newFixedThreadPool(4);

for (ChunkCoord coord : chunksToGenerate) {
    executor.submit(() -> generateChunk(coord.x, coord.z));
}
```

### 4. Early Exits
```java
// Skip expensive operations when possible
if (biome.getTreeDensity() == 0) {
    return; // No trees in desert
}

if (y < config.minHeight || y > config.maxHeight) {
    return; // Ore can't spawn here
}
```

---

## Common Issues and Solutions

### Issue: Biomes are too uniform
**Solution**: Increase noise octaves, add more variation to temperature/moisture

### Issue: Caves breach surface
**Solution**: Add surface distance check, reduce cave threshold near surface

### Issue: Trees floating/underground
**Solution**: Better surface detection, check if ground is solid

### Issue: Ore veins too scattered
**Solution**: Reduce angle variation in vein generator

### Issue: Chunk borders visible
**Solution**: Implement proper chunk blending, generate border regions

### Issue: Generation too slow
**Solution**: Profile code, cache noise values, use multithreading

---

## Advanced Features (Future)

### 1. Rivers
- Use noise to create river paths
- Carve channels through terrain
- Connect to oceans

### 2. Structures
- Villages
- Dungeons
- Temples
- Mineshafts

### 3. Weather Systems
- Biome-based weather
- Snow accumulation in cold biomes
- Rain particles

### 4. Dynamic Vegetation
- Grass spread
- Flowers based on biome
- Underwater plants

### 5. Improved Caves
- Underground lakes
- Stalactites/stalagmites
- Glowstone clusters
- Cave biomes

### 6. Ore Variants
- Ore clusters (multiple veins)
- Rare ore types
- Ore-rich biomes

---

## Configuration File Example

```java
public class WorldGenConfig {
    // Biome settings
    public static double TEMPERATURE_SCALE = 800.0;
    public static double MOISTURE_SCALE = 600.0;
    
    // Terrain settings
    public static double CONTINENT_SCALE = 2000.0;
    public static double TERRAIN_SCALE = 300.0;
    public static int WATER_LEVEL = 60;
    
    // Cave settings
    public static double CAVE_THRESHOLD = 0.6;
    public static double CAVERN_CHANCE = 0.05;
    
    // Tree settings
    public static double FOREST_TREE_DENSITY = 0.15;
    public static double PLAINS_TREE_DENSITY = 0.02;
    
    // Ore settings
    public static int DIAMOND_MIN_Y = 5;
    public static int DIAMOND_MAX_Y = 16;
    public static int COAL_VEINS_PER_CHUNK = 20;
    
    public static void loadFromFile(String path) {
        // Load config from JSON/properties file
    }
}
```

---

## Conclusion

This comprehensive world generation system creates rich, varied terrain with:

- **8 distinct biomes** with smooth transitions
- **Multiple tree types** that match their biomes
- **Complex cave systems** with both tunnels and caverns
- **Realistic ore distribution** with proper rarity

The modular design makes it easy to:
- Add new biomes
- Create new tree types
- Adjust cave generation
- Modify ore spawning
- Extend with new features

The system is optimized for performance while maintaining high-quality, natural-looking terrain that makes exploration exciting and rewarding.

Happy world building! 🌍⛏️
