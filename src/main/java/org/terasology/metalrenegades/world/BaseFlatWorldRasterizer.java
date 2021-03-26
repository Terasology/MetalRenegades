// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.metalrenegades.world;

import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.chunks.Chunk;
import org.terasology.engine.world.chunks.Chunks;
import org.terasology.engine.world.generation.Region;
import org.terasology.engine.world.generation.WorldRasterizer;
import org.terasology.engine.world.generation.facets.ElevationFacet;

public class BaseFlatWorldRasterizer implements WorldRasterizer {
    private Block dirt;

    @Override
    public void initialize() {
        dirt = CoreRegistry.get(BlockManager.class).getBlock("CoreAssets:Dirt");
    }

    @Override
    public void generateChunk(Chunk chunk, Region chunkRegion) {
        ElevationFacet elevationFacet = chunkRegion.getFacet(ElevationFacet.class);
        for (Vector3ic position : chunkRegion.getRegion()) {
            float surfaceHeight = elevationFacet.getWorld(position.x(), position.z());
            if (position.y() < surfaceHeight) {
                chunk.setBlock(Chunks.toRelative(position, new Vector3i()), dirt);
            }
        }
    }
}
