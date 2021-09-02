// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.metalrenegades.world.dynamic;

import org.joml.Vector2f;
import org.joml.Vector2ic;
import org.terasology.engine.utilities.procedural.BrownianNoise;
import org.terasology.engine.utilities.procedural.SimplexNoise;
import org.terasology.engine.utilities.procedural.SubSampledNoise;
import org.terasology.engine.world.generation.Border3D;
import org.terasology.engine.world.generation.ConfigurableFacetProvider;
import org.terasology.engine.world.generation.GeneratingRegion;
import org.terasology.engine.world.generation.Produces;
import org.terasology.engine.world.generation.ScalableFacetProvider;
import org.terasology.engine.world.generation.facets.SurfaceHumidityFacet;
import org.terasology.gestalt.entitysystem.component.Component;
import org.terasology.nui.properties.Range;

@Produces(SurfaceHumidityFacet.class)
public class HumidityProvider implements ConfigurableFacetProvider, ScalableFacetProvider {
    private static final int SAMPLE_RATE = 4;

    private SubSampledNoise noise;
    private Configuration config = new Configuration();
    private long seed;

    /**
     * Create with default values
     */
    public HumidityProvider() {
    }

    /**
     * Create with the given config
     * @param config the config to use
     */
    public HumidityProvider(Configuration config) {
        this.config = config;
    }

    @Override
    public void setSeed(long seed) {
        this.seed = seed;
        reload();
    }

    @Override
    public void process(GeneratingRegion region, float scale) {
        Border3D border = region.getBorderForFacet(SurfaceHumidityFacet.class);
        SurfaceHumidityFacet facet = new SurfaceHumidityFacet(region.getRegion(), border);

        float[] humidityData = facet.getInternal();
        float[] noiseData = noise.noise(facet.getWorldArea(), scale);
        for (int i = 0; i < humidityData.length; i++) {
            // The base humidity level goes from 0 to 0.3
            humidityData[i] = (noiseData[i] * 0.5f + 0.5f) * 0.3f;
        }

        region.setRegionFacet(SurfaceHumidityFacet.class, facet);
    }

    @Override
    public String getConfigurationName() {
        return "Humidity";
    }

    @Override
    public Component getConfiguration() {
        return config;
    }

    @Override
    public void setConfiguration(Component configuration) {
        this.config = (Configuration) configuration;
        reload();
    }

    /**
     * Change the noise generator depending on the changes in the config
     */
    private void reload() {
        float realScale = config.scale * 0.01f;
        Vector2f scale = new Vector2f(realScale, realScale);
        BrownianNoise brown = new BrownianNoise(new SimplexNoise(seed + 6), config.octaves);
        noise = new SubSampledNoise(brown, scale, SAMPLE_RATE);
    }

    private float getRandomHumidity(Vector2ic pos) {
        return 0.1f;
    }

    public static class Configuration implements Component<Configuration> {
        @Range(min = 0, max = 10, increment = 1, precision = 0, description = "The number of noise octaves")
        public int octaves = 5;

        @Range(min = 0.01f, max = 5f, increment = 0.01f, precision = 2, description = "The noise scale")
        public float scale = 0.08f;

        @Override
        public void copyFrom(Configuration other) {
            this.octaves = other.octaves;
            this.scale = other.scale;
        }
    }
}
