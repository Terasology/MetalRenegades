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
package org.terasology.metalrenegades.world.dynamic;

import org.terasology.core.world.generator.facetProviders.BiomeProvider;
import org.terasology.core.world.generator.facetProviders.DefaultFloraProvider;
import org.terasology.core.world.generator.facetProviders.SimplexHillsAndMountainsProvider;
import org.terasology.core.world.generator.facetProviders.SeaLevelProvider;
import org.terasology.core.world.generator.facetProviders.SurfaceToDensityProvider;
import org.terasology.core.world.generator.rasterizers.FloraRasterizer;
import org.terasology.dynamicCities.region.RegionEntityProvider;
import org.terasology.dynamicCities.region.ResourceProvider;
import org.terasology.dynamicCities.region.RoughnessProvider;
import org.terasology.dynamicCities.settlements.SettlementFacetProvider;
import org.terasology.dynamicCities.sites.SiteFacetProvider;
import org.terasology.dynamicCities.world.SolidRasterizer;
import org.terasology.dynamicCities.world.TreeRasterizer;
import org.terasology.dynamicCities.world.trees.DefaultTreeProvider;
import org.terasology.engine.SimpleUri;
import org.terasology.metalrenegades.world.dynamic.discoverables.DiscoverablesProvider;
import org.terasology.metalrenegades.world.dynamic.discoverables.DiscoverablesRasterizer;
import org.terasology.oreGeneration.generation.OreRasterizer;
import org.terasology.registry.In;
import org.terasology.world.generation.BaseFacetedWorldGenerator;
import org.terasology.world.generation.WorldBuilder;
import org.terasology.world.generator.RegisterWorldGenerator;
import org.terasology.world.generator.plugin.WorldGeneratorPluginLibrary;

@RegisterWorldGenerator(id = "dynamicWorld", displayName = "Dynamic World")
public class DynamicWorldGenerator extends BaseFacetedWorldGenerator {
    @In
    WorldGeneratorPluginLibrary worldGeneratorPluginLibrary;

    public DynamicWorldGenerator(SimpleUri uri) {
        super(uri);
    }

    @Override
    protected WorldBuilder createWorld() {
        int seaLevel = 0;

        return new WorldBuilder(worldGeneratorPluginLibrary)
                .addProvider(new SeaLevelProvider(seaLevel))
                .addProvider(new SurfaceProvider())
                .addProvider(new HumidityProvider())
                .addProvider(new TemperatureProvider())
                .addProvider(new SurfaceToDensityProvider())
                .addProvider(new SimplexHillsAndMountainsProvider())
                .addProvider(new BiomeProvider())
                .addProvider(new DefaultFloraProvider())
                .addProvider(new DefaultTreeProvider())
                .addProvider(new ResourceProvider())
                .addProvider(new RoughnessProvider())
                .addProvider(new DiscoverablesProvider())
                .addProvider(new SiteFacetProvider())
                .addProvider(new SettlementFacetProvider())
                .addEntities(new RegionEntityProvider())
                .addRasterizer(new SolidRasterizer())
                .addRasterizer(new FloraRasterizer())
                .addRasterizer(new TreeRasterizer())
                .addRasterizer(new OreRasterizer())
                .addRasterizer(new DiscoverablesRasterizer())
                .addPlugins();
    }
}
