// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.metalrenegades.world.dynamic;

import org.joml.Vector2ic;
import org.terasology.biomesAPI.Biome;
import org.terasology.core.world.CoreBiome;
import org.terasology.core.world.generator.facets.BiomeFacet;
import org.terasology.engine.utilities.procedural.WhiteNoise;
import org.terasology.engine.world.generation.Border3D;
import org.terasology.engine.world.generation.Facet;
import org.terasology.engine.world.generation.GeneratingRegion;
import org.terasology.engine.world.generation.Produces;
import org.terasology.engine.world.generation.UpdatePriority;
import org.terasology.engine.world.generation.Updates;
import org.terasology.engine.world.generation.ScalableFacetProvider;
import org.terasology.engine.world.generation.facets.SurfaceHumidityFacet;

import java.util.Iterator;

/**
 * The basic biome provider for Metal Renegades.
 * Fills 2/3 of the world with desert, the rest with scrubland, based on the humidity.
 * Providers for features like rivers and mountains will adjust it further.
 */
@Produces(BiomeFacet.class)
@Updates(value = @Facet(SurfaceHumidityFacet.class), priority = UpdatePriority.PRIORITY_CRITICAL)
public class BaseBiomeProvider implements ScalableFacetProvider {

    private WhiteNoise whiteNoise;

    @Override
    public void setSeed(long seed) {
        whiteNoise = new WhiteNoise((int) (seed % Integer.MAX_VALUE) - 2);

        // Give the seed to the biomes - this is essentially mutable global state, and only works because there's only one world at a time
        // To support multi-world, the way this works will need to be changed
        for (MRBiome b : MRBiome.values()) {
            b.setSeed(seed);
        }
    }

    @Override
    public void process(GeneratingRegion region, float scale) {
        Border3D border = region.getBorderForFacet(BiomeFacet.class);
        BiomeFacet biomes = new BiomeFacet(region.getRegion(), border);
        SurfaceHumidityFacet humidityFacet = region.getRegionFacet(SurfaceHumidityFacet.class);

        float[] humidityData = humidityFacet.getInternal();
        Biome[] biomeData = biomes.getInternal();
        Iterator<Vector2ic> positions = biomes.getWorldArea().iterator();
        for (int i = 0; i < biomeData.length; i++) {
            Vector2ic pos = positions.next();
            // humidity goes from 0 to 0.3, so there's a 2/3 chance of desert
            if (humidityData[i] > 0.2 + whiteNoise.noise(pos.x() * scale, pos.y() * scale) * 0.01) {
                biomeData[i] = MRBiome.SCRUBLAND;
            } else {
                biomeData[i] = CoreBiome.DESERT;
            }
        }

        region.setRegionFacet(BiomeFacet.class, biomes);
    }
}
