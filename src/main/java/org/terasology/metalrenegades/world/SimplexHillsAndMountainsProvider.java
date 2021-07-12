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
import org.terasology.engine.world.generation.Requires;
import org.terasology.engine.world.generation.Updates;
import org.terasology.engine.world.generation.facets.ElevationFacet;
import org.terasology.engine.world.generation.facets.SurfaceHumidityFacet;
import org.terasology.math.TeraMath;
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
@Requires(@Facet(SurfaceHumidityFacet.class))
@Updates({@Facet(ElevationFacet.class), @Facet(BiomeFacet.class)})
public class SimplexHillsAndMountainsProvider implements ConfigurableFacetProvider {

    private SubSampledNoise mountainNoise;
    private SubSampledNoise hillNoise;
    private SubSampledNoise mountainIntensityNoise;
    private SubSampledNoise mesaNoise;
    private SubSampledNoise mesaHeightNoise;
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
        mesaNoise = new SubSampledNoise(
                new BrownianNoise(new SimplexNoise(seed + 14), 6),
                new Vector2f(0.001f, 0.001f), 4);
        mesaHeightNoise = new SubSampledNoise(
                new BrownianNoise(new SimplexNoise(seed + 15), 3),
                new Vector2f(0.0002f, 0.0002f), 4);
        whiteNoise = new WhiteNoise((int) (seed % Integer.MAX_VALUE) - 1);
    }

    @Override
    public void process(GeneratingRegion region) {
        ElevationFacet facet = region.getRegionFacet(ElevationFacet.class);
        BiomeFacet biomes = region.getRegionFacet(BiomeFacet.class);
        SurfaceHumidityFacet humidityFacet = region.getRegionFacet(SurfaceHumidityFacet.class);

        float[] mountainData = mountainNoise.noise(facet.getWorldArea());
        float[] hillData = hillNoise.noise(facet.getWorldArea());
        float[] mountainIntensityData = mountainIntensityNoise.noise(facet.getWorldArea());
        float[] mesaData = mesaNoise.noise(facet.getWorldArea());
        float[] mesaHeightData = mesaHeightNoise.noise(facet.getWorldArea());

        float[] humidityData = humidityFacet.getInternal();
        Biome[] biomeData = biomes.getInternal();
        float[] heightData = facet.getInternal();
        Iterator<Vector2ic> positions = facet.getWorldArea().iterator();
        for (int i = 0; i < heightData.length; ++i) {
            // Only place mesas at humidity below humidMax, since they only occur in dry areas like deserts
            float humidMax = 0.18f;
            float humidSteepness = 10;
            float baseMesaNoise =
                    mesaData[i] * TeraMath.fadePerlin(TeraMath.clamp(humidSteepness - humidSteepness / humidMax * humidityData[i]));
            // Noise values between threshold and threshold+smoothness are on the side of a mesa
            // Noise values smaller than threshold are nothing, and larger than threshold+smoothness are on a steppe
            // Like rivers, a graph is available to see the details: https://www.desmos.com/calculator/dc04yalbds
            float threshold = 0.5f;
            float smoothness = 0.05f;
            float mesaH = TeraMath.fadePerlin(TeraMath.clamp((baseMesaNoise - threshold) / smoothness));
            // Create a slope up to the bottom of the mesa
            float slopeSteepness = 5;
            float slopeHeight = 0.7f; // values from 0.5 - 1.5 look fine, might be noise later
            float mesaL = TeraMath.clamp(slopeSteepness * (baseMesaNoise - threshold - 0.5f * smoothness) + 1);
            float mesa = (mesaH + slopeHeight * mesaL) / (1 + slopeHeight);

            float mountainIntensity = mountainIntensityData[i] * 0.5f + 0.5f;
            float densityMountains = Math.max(mountainData[i] * 2.12f, 0) * mountainIntensity * configuration.mountainAmplitude;
            float densityHills =
                    Math.max(hillData[i] * 2.12f - 0.1f, 0) * (1.0f - mountainIntensity) * configuration.hillAmplitude;

            heightData[i] = heightData[i]
                    // Get rid of mountains and hills around mesas, so they don't interfere
                    + (256 * densityMountains + 64 * densityHills) * TeraMath.clamp(2 - 3 * baseMesaNoise)
                    + (64 + 32 * mesaHeightData[i]) * mesa;
            Vector2ic pos = positions.next();
            if (Math.max(mesa, densityMountains) > 0.1 + whiteNoise.noise(pos.x(), pos.y()) * 0.02) {
                biomeData[i] = MRBiome.ROCKY;
                if (mesa > 0.999) {
                    biomeData[i] = MRBiome.STEPPE;
                }
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
