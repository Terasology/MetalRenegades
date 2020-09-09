// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.metalrenegades.world.dynamic;

import org.terasology.coreworlds.generator.facetProviders.BiomeProvider;
import org.terasology.coreworlds.generator.facetProviders.DefaultFloraProvider;
import org.terasology.coreworlds.generator.facetProviders.SeaLevelProvider;
import org.terasology.coreworlds.generator.facetProviders.SimplexHillsAndMountainsProvider;
import org.terasology.coreworlds.generator.facetProviders.SurfaceToDensityProvider;
import org.terasology.coreworlds.generator.rasterizers.FloraRasterizer;
import org.terasology.dynamicCities.region.RegionEntityProvider;
import org.terasology.dynamicCities.region.ResourceProvider;
import org.terasology.dynamicCities.region.RoughnessProvider;
import org.terasology.dynamicCities.settlements.SettlementFacetProvider;
import org.terasology.dynamicCities.sites.SiteFacetProvider;
import org.terasology.dynamicCities.world.SolidRasterizer;
import org.terasology.dynamicCities.world.TreeRasterizer;
import org.terasology.dynamicCities.world.trees.DefaultTreeProvider;
import org.terasology.engine.core.SimpleUri;
import org.terasology.engine.registry.In;
import org.terasology.engine.world.generation.BaseFacetedWorldGenerator;
import org.terasology.engine.world.generation.WorldBuilder;
import org.terasology.engine.world.generator.RegisterWorldGenerator;
import org.terasology.engine.world.generator.plugin.WorldGeneratorPluginLibrary;
import org.terasology.metalrenegades.world.dynamic.discoverables.DiscoverablesProvider;
import org.terasology.metalrenegades.world.dynamic.discoverables.DiscoverablesRasterizer;
import org.terasology.oreGeneration.generation.OreRasterizer;

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
