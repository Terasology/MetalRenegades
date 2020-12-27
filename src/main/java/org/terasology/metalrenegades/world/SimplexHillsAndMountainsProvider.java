// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.metalrenegades.world;

import org.terasology.entitySystem.Component;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.BaseVector2i;
import org.terasology.math.geom.Vector2f;
import org.terasology.nui.properties.Range;
import org.terasology.utilities.procedural.BrownianNoise;
import org.terasology.utilities.procedural.SimplexNoise;
import org.terasology.utilities.procedural.SubSampledNoise;
import org.terasology.world.generation.ConfigurableFacetProvider;
import org.terasology.world.generation.Facet;
import org.terasology.world.generation.GeneratingRegion;
import org.terasology.world.generation.Requires;
import org.terasology.world.generation.Updates;
import org.terasology.world.generation.facets.ElevationFacet;
import org.terasology.world.generation.facets.SurfaceHumidityFacet;
import org.terasology.world.generation.facets.SurfaceTemperatureFacet;

import java.util.Iterator;

/**
 * Adds surface height for hill and mountain regions. Mountain and hill regions are based off of temperature and humidity.
 *
 * This was moved to MetalRenegades from CoreWorlds because the CoreWorlds generator no longer needs it.
 * It would be good to refactor this away at some point, to bring the MetalRenegades world generator
 * more in line with the CoreWorlds one.
 */
@Requires({@Facet(SurfaceTemperatureFacet.class), @Facet(SurfaceHumidityFacet.class)})
@Updates(@Facet(ElevationFacet.class))
public class SimplexHillsAndMountainsProvider implements ConfigurableFacetProvider {

    private SubSampledNoise mountainNoise;
    private SubSampledNoise hillNoise;
    private SimplexHillsAndMountainsProviderConfiguration configuration = new SimplexHillsAndMountainsProviderConfiguration();

    @Override
    public void setSeed(long seed) {
        // TODO: reduce the number of octaves in BrownianNoise
        mountainNoise = new SubSampledNoise(new BrownianNoise(new SimplexNoise(seed + 3)), new Vector2f(0.0002f, 0.0002f), 4);
        hillNoise = new SubSampledNoise(new BrownianNoise(new SimplexNoise(seed + 4)), new Vector2f(0.0008f, 0.0008f), 4);
    }

    @Override
    public void process(GeneratingRegion region) {
        ElevationFacet facet = region.getRegionFacet(ElevationFacet.class);

        float[] mountainData = mountainNoise.noise(facet.getWorldRegion());
        float[] hillData = hillNoise.noise(facet.getWorldRegion());
        SurfaceTemperatureFacet temperatureData = region.getRegionFacet(SurfaceTemperatureFacet.class);
        SurfaceHumidityFacet humidityData = region.getRegionFacet(SurfaceHumidityFacet.class);

        float[] heightData = facet.getInternal();
        Iterator<BaseVector2i> positionIterator = facet.getRelativeRegion().contents().iterator();
        for (int i = 0; i < heightData.length; ++i) {
            BaseVector2i pos = positionIterator.next();
            float temp = temperatureData.get(pos);
            float tempHumid = temp * humidityData.get(pos);
            Vector2f distanceToMountainBiome = new Vector2f(temp - 0.25f, tempHumid - 0.35f);
            float mIntens = TeraMath.clamp(1.0f - distanceToMountainBiome.length() * 3.0f);
            float densityMountains = Math.max(mountainData[i] * 2.12f, 0) * mIntens * configuration.mountainAmplitude;
            float densityHills = Math.max(hillData[i] * 2.12f - 0.1f, 0) * (1.0f - mIntens) * configuration.hillAmplitude;

            heightData[i] = heightData[i] + 1024 * densityMountains + 128 * densityHills;
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
