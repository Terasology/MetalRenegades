// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.metalrenegades.world.dynamic;

import org.joml.Math;
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
     */
    private Block[] rocks;

    MRBiome(String displayName) {
        this.id = new Name("MetalRenegades:" + name());
        this.displayName = displayName;

        BlockManager blockManager = CoreRegistry.get(BlockManager.class);
        stone = blockManager.getBlock("CoreAssets:stone");
        sand = blockManager.getBlock("CoreAssets:Sand");
        grass = blockManager.getBlock("CoreAssets:Grass");
        snow = blockManager.getBlock("CoreAssets:Snow");
        dirt = blockManager.getBlock("CoreAssets:Dirt");
        Block sandstone = blockManager.getBlock("GenericRocks:Sandstone");
        // Sandstone is the substrate that the other strata are in
        // The most efficient way to achieve that is to just enter it multiple times in the array
        rocks = new Block[]{
                sandstone,
                sandstone,
                sandstone,
                sandstone,
                stone,
                blockManager.getBlock("GenericRocks:HardenedClay"),
                sandstone,
                sandstone,
                sandstone,
                blockManager.getBlock("GenericRocks:Limestone"),
                blockManager.getBlock("GenericRocks:Slate"),
                sandstone,
                sandstone,
                sandstone,
        };
    }

    public void setSeed(long seed) {
        // This noise will be directly used as an index in the `rocks` array for strata
        // The horizontal layout is achieved by making the noise change much more rapidly in the y coordinate
        strataNoise = new SubSampledNoise(
                new BrownianNoise(new SimplexNoise(seed + 17), 1),
                new Vector3f(0.005f, 0.4f, 0.005f), 4);
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
            case STEPPE:
                // The steppe has less dirt since it's often visible on the side of mesas
                if (density > 2) {
                    return getStratum(pos);
                } else {
                    return dirt;
                }
            default:
                if (density > 8) {
                    return getStratum(pos);
                } else {
                    return dirt;
                }
        }
    }

    private Block getStratum(Vector3ic pos) {
        // Noise, adjusted to the interval [0,1]
        float noise = strataNoise.noise(pos.x(), pos.y(), pos.z()) * 0.5f + 0.5f;
        // Now pick an entry in `rocks` based on the noise
        int idx = Math.clamp(0, rocks.length - 1, (int) (noise * rocks.length));
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
