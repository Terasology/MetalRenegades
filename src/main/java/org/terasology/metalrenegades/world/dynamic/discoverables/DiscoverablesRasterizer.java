// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.metalrenegades.world.dynamic.discoverables;

import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.math.ChunkMath;
import org.terasology.math.Region3i;
import org.terasology.math.geom.BaseVector3i;
import org.terasology.math.geom.Vector3i;
import org.terasology.registry.CoreRegistry;
import org.terasology.registry.In;
import org.terasology.structureTemplates.components.SpawnBlockRegionsComponent;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.chunks.CoreChunk;
import org.terasology.world.generation.Region;
import org.terasology.world.generation.WorldRasterizer;

import java.util.Map;
import java.util.Objects;

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

    private Prefab structure;

    @Override
    public void initialize() {
        chest = CoreRegistry.get(BlockManager.class).getBlock("MetalRenegades:hiddenChest.RIGHT");
        structure = Objects.requireNonNull(CoreRegistry.get(PrefabManager.class)).getPrefab("MetalRenegades:wellTemplate");

        entityManager = CoreRegistry.get(EntityManager.class);
    }

    @Override
    public void generateChunk(CoreChunk chunk, Region chunkRegion) {
        DiscoverablesFacet discoverablesFacet = chunkRegion.getFacet(DiscoverablesFacet.class);

        SpawnBlockRegionsComponent spawnBlockRegionsComponent =
                structure.getComponent(SpawnBlockRegionsComponent.class);

        for (Map.Entry<BaseVector3i, DiscoverablesChest> entry : discoverablesFacet.getWorldEntries().entrySet()) {
            Vector3i structurePosition = new Vector3i(entry.getKey());

            for (SpawnBlockRegionsComponent.RegionToFill regionToFill : spawnBlockRegionsComponent.regionsToFill) {
                  Block block = regionToFill.blockType;

                  Region3i region = regionToFill.region;
                  for (Vector3i pos : region) {
                      pos.add(structurePosition);
                      if (chunkRegion.getRegion().encompasses(pos)) {
                          chunk.setBlock(ChunkMath.calcRelativeBlockPos(pos), block);
                      }
                  }
            }
        }
    }


}
