// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.metalrenegades.world.dynamic;

import org.joml.Vector2f;
import org.joml.Vector2ic;
import org.terasology.engine.utilities.procedural.BrownianNoise;
import org.terasology.engine.utilities.procedural.PerlinNoise;
import org.terasology.engine.utilities.procedural.SubSampledNoise;
import org.terasology.engine.world.generation.Border3D;
import org.terasology.engine.world.generation.ConfigurableFacetProvider;
import org.terasology.engine.world.generation.GeneratingRegion;
import org.terasology.engine.world.generation.Produces;
import org.terasology.engine.world.generation.facets.SurfaceHumidityFacet;
import org.terasology.gestalt.entitysystem.component.Component;
import org.terasology.nui.properties.Range;

@Produces(SurfaceHumidityFacet.class)
public class HumidityProvider implements ConfigurableFacetProvider {
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
     * @param config: the config to use
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
    public void process(GeneratingRegion region) {
        Border3D border = region.getBorderForFacet(SurfaceHumidityFacet.class);
        SurfaceHumidityFacet facet = new SurfaceHumidityFacet(region.getRegion(), border);

        // TODO: Setup humidity
        for (Vector2ic position: facet.getWorldArea()) {
            double hum = getRandomHumidity(position);
            facet.setWorld(position, (float) hum);
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
        BrownianNoise brown = new BrownianNoise(new PerlinNoise(seed + 6), config.octaves);
        noise = new SubSampledNoise(brown, scale, SAMPLE_RATE);
    }

    private float getRandomHumidity(Vector2ic pos) {
        return 0.1f;
    }

    public static class Configuration implements Component<Configuration> {
        @Range(min = 0, max = 10.0f, increment = 1f, precision = 0, description = "The number of noise octaves")
        public int octaves = 8;

        @Range(min = 0.01f, max = 5f, increment = 0.01f, precision = 2, description = "The noise scale")
        public float scale = 0.05f;
    }
}
