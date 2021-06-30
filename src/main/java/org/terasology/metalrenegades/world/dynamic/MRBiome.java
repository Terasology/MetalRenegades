// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.metalrenegades.world.dynamic;

import org.joml.Vector3f;
import org.joml.Vector3ic;
import org.terasology.biomesAPI.Biome;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.utilities.procedural.BrownianNoise;
import org.terasology.engine.utilities.procedural.SimplexNoise;
import org.terasology.engine.utilities.procedural.SubSampledNoise;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.gestalt.naming.Name;

/**
 * The new biomes added by Metal Renegades. On top of these, MR reuses CoreBiome.DESERT.
 */
public enum MRBiome implements Biome {
    ROCKY("Rocky"),
    SCRUBLAND("Scrubland"),
    RIVER("Riparian"),
    STEPPE("Steppe");

    private final Name id;
    private final String displayName;

    private SubSampledNoise strataNoise;

    private Block stone;
    private Block sand;
    private Block grass;
    private Block snow;
    private Block dirt;
    /**
     * All the rock types that can be used in strata.
     * Strata always come in the same order, and wrap around at the end.
     */
    private Block[] rocks;

    MRBiome(String displayName) {
        this.id = new Name("MetalRenegades:" + name());
        this.displayName = displayName;

        strataNoise = new SubSampledNoise(
                new BrownianNoise(new SimplexNoise(2), 4),
                new Vector3f(0.002f, 0.0005f, 0.002f), 4);

        BlockManager blockManager = CoreRegistry.get(BlockManager.class);
        stone = blockManager.getBlock("CoreAssets:stone");
        sand = blockManager.getBlock("CoreAssets:Sand");
        grass = blockManager.getBlock("CoreAssets:Grass");
        snow = blockManager.getBlock("CoreAssets:Snow");
        dirt = blockManager.getBlock("CoreAssets:Dirt");
        rocks = new Block[]{
                blockManager.getBlock("MetalRenegades:sandstone"),
                stone,
                blockManager.getBlock("GenericRocks:HardenedClay"),
                blockManager.getBlock("GenericRocks:Limestone"),
                blockManager.getBlock("GenericRocks:Slate"),
        };
    }

    @Override
    public Block getSurfaceBlock(Vector3ic pos, int seaLevel) {
        switch (this) {
            case ROCKY:
                return getStratum(pos);
            case SCRUBLAND:
                return dirt;
            case RIVER:
                if (pos.y() < seaLevel) {
                    // Don't put grass under water
                    return sand;
                } else {
                    return grass;
                }
            case STEPPE:
                return grass;
            default:
                return dirt;
        }
    }

    @Override
    public Block getBelowSurfaceBlock(Vector3ic pos, float density) {
        switch (this) {
            case ROCKY:
                return getStratum(pos);
            default:
                if (density > 8) {
                    return stone;
                } else {
                    return dirt;
                }
        }
    }

    private Block getStratum(Vector3ic pos) {
        // Generate a strata index with a modulus of the y coordinate, adjusted with noise
        // Strata are 20 (1/0.05) blocks high on average, but it varies since the noise value depends on height
        float noise = strataNoise.noise(pos.x(), pos.y(), pos.z());
        int idx = (int) Math.abs((float) pos.y() * 0.05 + noise * rocks.length + 1000) % rocks.length;
        return rocks[idx];
    }

    @Override
    public Name getId() {
        return id;
    }

    @Override
    public String getDisplayName() {
        return this.displayName;
    }

    @Override
    public String toString() {
        return this.displayName;
    }
}
