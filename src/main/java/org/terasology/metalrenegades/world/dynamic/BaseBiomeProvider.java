// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.metalrenegades.world.dynamic;

import org.joml.Vector2f;
import org.terasology.biomesAPI.Biome;
import org.terasology.core.world.CoreBiome;
import org.terasology.core.world.generator.facets.BiomeFacet;
import org.terasology.engine.utilities.procedural.BrownianNoise;
import org.terasology.engine.utilities.procedural.SimplexNoise;
import org.terasology.engine.utilities.procedural.SubSampledNoise;
import org.terasology.engine.world.generation.Border3D;
import org.terasology.engine.world.generation.FacetProvider;
import org.terasology.engine.world.generation.GeneratingRegion;
import org.terasology.engine.world.generation.Produces;

/**
 * The basic biome provider for Metal Renegades.
 * Fills 65% of the world with desert, the rest with scrubland.
 * Providers for features like rivers and mountains will adjust it further.
 */
@Produces(BiomeFacet.class)
public class BaseBiomeProvider implements FacetProvider {

    private SubSampledNoise biomeNoise;

    @Override
    public void setSeed(long seed) {
        biomeNoise = new SubSampledNoise(
                new BrownianNoise(new SimplexNoise(seed + 9), 5),
                new Vector2f(0.0008f, 0.0008f), 4);
    }

    @Override
    public void process(GeneratingRegion region) {
        Border3D border = region.getBorderForFacet(BiomeFacet.class);
        BiomeFacet biomes = new BiomeFacet(region.getRegion(), border);

        float[] noiseData = biomeNoise.noise(biomes.getWorldArea());
        Biome[] biomeData = biomes.getInternal();
        for (int i = 0; i < biomeData.length; i++) {
            // noiseData[i] goes from -1 to 1, so there's a 65% chance of desert
            if (noiseData[i] > 0.3) {
                biomeData[i] = MRBiome.SCRUBLAND;
            } else {
                biomeData[i] = CoreBiome.DESERT;
            }
        }

        region.setRegionFacet(BiomeFacet.class, biomes);
    }
}
