// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.metalrenegades.world;

import org.joml.Vector2f;
import org.joml.Vector2ic;
import org.terasology.biomesAPI.Biome;
import org.terasology.core.world.generator.facets.BiomeFacet;
import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.utilities.procedural.BrownianNoise;
import org.terasology.engine.utilities.procedural.SimplexNoise;
import org.terasology.engine.utilities.procedural.SubSampledNoise;
import org.terasology.engine.utilities.procedural.WhiteNoise;
import org.terasology.engine.world.generation.ConfigurableFacetProvider;
import org.terasology.engine.world.generation.Facet;
import org.terasology.engine.world.generation.GeneratingRegion;
import org.terasology.engine.world.generation.Updates;
import org.terasology.engine.world.generation.facets.ElevationFacet;
import org.terasology.metalrenegades.world.dynamic.MRBiome;
import org.terasology.nui.properties.Range;

import java.util.Iterator;

/**
 * Adds surface height for hill and mountain regions.
 *
 * This was moved to MetalRenegades from CoreWorlds because the CoreWorlds generator no longer needs it.
 * It would be good to refactor this away at some point, to bring the MetalRenegades world generator
 * more in line with the CoreWorlds one.
 *
 * It also sets the biome to the rocky biome on mountains.
 */
@Updates({@Facet(ElevationFacet.class), @Facet(BiomeFacet.class)})
public class SimplexHillsAndMountainsProvider implements ConfigurableFacetProvider {

    private SubSampledNoise mountainNoise;
    private SubSampledNoise hillNoise;
    private SubSampledNoise mountainIntensityNoise;
    private SimplexHillsAndMountainsProviderConfiguration configuration =
            new SimplexHillsAndMountainsProviderConfiguration();
    private WhiteNoise whiteNoise;

    @Override
    public void setSeed(long seed) {
        mountainNoise = new SubSampledNoise(
                new BrownianNoise(new SimplexNoise(seed + 3), 8),
                new Vector2f(0.0005f, 0.0005f), 4);
        hillNoise = new SubSampledNoise(
                new BrownianNoise(new SimplexNoise(seed + 4), 6),
                new Vector2f(0.0008f, 0.0008f), 4);
        mountainIntensityNoise = new SubSampledNoise(
                new BrownianNoise(new SimplexNoise(seed + 5), 4),
                new Vector2f(0.00005f, 0.00005f), 4);
        whiteNoise = new WhiteNoise((int) (seed % Integer.MAX_VALUE) - 1);
    }

    @Override
    public void process(GeneratingRegion region) {
        ElevationFacet facet = region.getRegionFacet(ElevationFacet.class);
        BiomeFacet biomes = region.getRegionFacet(BiomeFacet.class);

        float[] mountainData = mountainNoise.noise(facet.getWorldArea());
        float[] hillData = hillNoise.noise(facet.getWorldArea());
        float[] mountainIntensityData = mountainIntensityNoise.noise(facet.getWorldArea());

        Biome[] biomeData = biomes.getInternal();
        float[] heightData = facet.getInternal();
        Iterator<Vector2ic> positions = facet.getWorldArea().iterator();
        for (int i = 0; i < heightData.length; ++i) {
            float mountainIntensity = mountainIntensityData[i] * 0.5f + 0.5f;
            float densityMountains = Math.max(mountainData[i] * 2.12f, 0) * mountainIntensity * configuration.mountainAmplitude;
            float densityHills =
                    Math.max(hillData[i] * 2.12f - 0.1f, 0) * (1.0f - mountainIntensity) * configuration.hillAmplitude;

            heightData[i] = heightData[i] + 256 * densityMountains + 64 * densityHills;
            Vector2ic pos = positions.next();
            if (densityMountains > 0.1 + whiteNoise.noise(pos.x(), pos.y()) * 0.02) {
                biomeData[i] = MRBiome.ROCKY;
            }
        }
    }

    @Override
    public String getConfigurationName() {
        return "Hills and Mountains";
    }

    @Override
    public Component getConfiguration() {
        return configuration;
    }

    @Override
    public void setConfiguration(Component configuration) {
        this.configuration = (SimplexHillsAndMountainsProviderConfiguration) configuration;
    }

    private static class SimplexHillsAndMountainsProviderConfiguration implements Component {

        @Range(min = 0, max = 3f, increment = 0.01f, precision = 2, description = "Mountain Amplitude")
        public float mountainAmplitude = 1f;

        @Range(min = 0, max = 2f, increment = 0.01f, precision = 2, description = "Hill Amplitude")
        public float hillAmplitude = 1f;
    }
}
