// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.metalrenegades.world.dynamic.roads;

import org.terasology.cities.BlockTheme;
import org.terasology.cities.DefaultBlockType;
import org.terasology.dynamicCities.construction.Construction;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.registry.In;
import org.terasology.registry.Share;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.BlockManager;

/**
 * Sets up custom rasterizers for the construction system.
 */
@Share(value = RoadManager.class)
@RegisterSystem(RegisterMode.AUTHORITY)
public class RoadManager extends BaseComponentSystem {

    @In
    private Construction construction;

    @In
    private BlockManager blockManager;

    @In
    private WorldProvider worldProvider;

    @Override
    public void initialise() {
        BlockTheme theme = BlockTheme.builder(blockManager)
                .register(DefaultBlockType.ROAD_SURFACE, "CoreAssets:Gravel")
                .register(DefaultBlockType.ROAD_FILL, "CoreAssets:Dirt")
                .register(RailBlockType.BASE, "CoreAssets:Gravel")
                .registerFamily(RailBlockType.RAIL, "Rails:rails")
                .build();

        construction.setRoadRasterizer(new RailRasterizer(worldProvider), theme);
    }
}
