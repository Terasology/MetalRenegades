// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.metalrenegades.world.dynamic.discoverables;

import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.math.ChunkMath;
import org.terasology.math.geom.BaseVector3i;
import org.terasology.math.geom.Vector3i;
import org.terasology.registry.CoreRegistry;
import org.terasology.registry.In;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.chunks.CoreChunk;
import org.terasology.world.generation.Region;
import org.terasology.world.generation.WorldRasterizer;

import java.util.Map;

/**
 * Places discoverable chests in the world at the positions specified in {@link DiscoverablesFacet}.
 */
public class DiscoverablesRasterizer implements WorldRasterizer {

    @In
    private EntityManager entityManager;

    /**
     * The Metal Renegades specific hidden chest block. This chest block is defined specifically in MR because this
     * block must contain a {@link DiscoverableChestComponent}, and components cannot be added on world gen.
     */
    private Block chest;

    @Override
    public void initialize() {
        chest = CoreRegistry.get(BlockManager.class).getBlock("MetalRenegades:hiddenChest.RIGHT");
        entityManager = CoreRegistry.get(EntityManager.class);
    }

    @Override
    public void generateChunk(CoreChunk chunk, Region chunkRegion) {
        DiscoverablesFacet discoverablesFacet = chunkRegion.getFacet(DiscoverablesFacet.class);

        for (Map.Entry<BaseVector3i, DiscoverablesChest> entry : discoverablesFacet.getWorldEntries().entrySet()) {
            Vector3i chestPosition = new Vector3i(entry.getKey());
            chunk.setBlock(ChunkMath.calcRelativeBlockPos(chestPosition), chest);
        }
    }


}
