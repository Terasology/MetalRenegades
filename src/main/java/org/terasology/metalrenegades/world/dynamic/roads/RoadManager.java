/*
 * Copyright 2019 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
                .register(DefaultBlockType.ROAD_SURFACE, "CoreBlocks:Gravel")
                .register(DefaultBlockType.ROAD_FILL, "CoreBlocks:Dirt")
                .register(RailBlockType.BASE, "CoreBlocks:Gravel")
                .registerFamily(RailBlockType.RAIL, "Rails:rails")
                .build();

        construction.setRoadRasterizer(new RailRasterizer(worldProvider), theme);
    }
}
