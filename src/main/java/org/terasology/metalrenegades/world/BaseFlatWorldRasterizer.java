// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.metalrenegades.world;

import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.terasology.registry.CoreRegistry;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.chunks.Chunks;
import org.terasology.world.chunks.CoreChunk;
import org.terasology.world.generation.Region;
import org.terasology.world.generation.WorldRasterizer;
import org.terasology.world.generation.facets.ElevationFacet;

public class BaseFlatWorldRasterizer implements WorldRasterizer {
    private Block dirt;

    @Override
    public void initialize() {
        dirt = CoreRegistry.get(BlockManager.class).getBlock("CoreAssets:Dirt");
    }

    @Override
    public void generateChunk(CoreChunk chunk, Region chunkRegion) {
        ElevationFacet elevationFacet = chunkRegion.getFacet(ElevationFacet.class);
        for (Vector3ic position : chunkRegion.getRegion()) {
            float surfaceHeight = elevationFacet.getWorld(position.x(), position.z());
            if (position.y() < surfaceHeight) {
                chunk.setBlock(Chunks.toRelative(position, new Vector3i()), dirt);
            }
        }
    }
}
