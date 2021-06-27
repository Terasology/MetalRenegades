// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.metalrenegades.world.dynamic;

import org.joml.Vector3ic;
import org.terasology.biomesAPI.Biome;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.gestalt.naming.Name;

/**
 * The new biomes added by Metal Renegades.
 * On top of these, MR reuses CoreBiome.DESERT.
 */
public enum MRBiome implements Biome {
    ROCKY("Rocky"),
    SCRUBLAND("Scrubland"),
    RIVER("Riparian"),
    STEPPE("Steppe");

    private final Name id;
    private final String displayName;

    private Block stone;
    private Block sand;
    private Block grass;
    private Block snow;
    private Block dirt;

    MRBiome(String displayName) {
        this.id = new Name("MetalRenegades:" + name());
        this.displayName = displayName;

        BlockManager blockManager = CoreRegistry.get(BlockManager.class);
        stone = blockManager.getBlock("CoreAssets:stone");
        sand = blockManager.getBlock("CoreAssets:Sand");
        grass = blockManager.getBlock("CoreAssets:Grass");
        snow = blockManager.getBlock("CoreAssets:Snow");
        dirt = blockManager.getBlock("CoreAssets:Dirt");
    }

    @Override
    public Block getSurfaceBlock(Vector3ic pos, int seaLevel) {
        switch (this) {
            case ROCKY:
                return stone;
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
                return stone;
            default:
                if (density > 8) {
                    return stone;
                } else {
                    return dirt;
                }
        }
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
