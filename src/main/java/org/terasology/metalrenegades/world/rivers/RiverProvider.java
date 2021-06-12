// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.metalrenegades.world.rivers;

import org.joml.Vector2f;
import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.utilities.procedural.BrownianNoise;
import org.terasology.engine.utilities.procedural.SimplexNoise;
import org.terasology.engine.utilities.procedural.SubSampledNoise;
import org.terasology.engine.world.generation.ConfigurableFacetProvider;
import org.terasology.engine.world.generation.GeneratingRegion;
import org.terasology.engine.world.generation.Produces;
import org.terasology.engine.world.generation.ScalableFacetProvider;
import org.terasology.nui.properties.Range;

@Produces(RiverFacet.class)
public class RiverProvider implements ScalableFacetProvider, ConfigurableFacetProvider {
    private static final int SAMPLE_RATE = 4;

    private long seed;
    private SubSampledNoise riverNoise;
    private Configuration configuration = new Configuration();

    @Override
    public void setSeed(long seed) {
        this.seed = seed;
        riverNoise = new SubSampledNoise(
                new BrownianNoise(new SimplexNoise(seed + 2), 8),
                new Vector2f(0.0008f * configuration.riverDensity, 0.0008f * configuration.riverDensity), SAMPLE_RATE);
    }

    @Override
    public void process(GeneratingRegion region, float scale) {
        RiverFacet facet = new RiverFacet(region.getRegion(), region.getBorderForFacet(RiverFacet.class),
                configuration.maxDepth);
        float[] noise = riverNoise.noise(facet.getWorldArea(), scale);

        float[] rivers = facet.getInternal();
        float noiseMult = 10f / (configuration.riverWidth * configuration.riverDensity);
        for (int i = 0; i < noise.length; ++i) {
            rivers[i] = -Math.min(0, Math.abs(noise[i]) * noiseMult - 1);
        }

        region.setRegionFacet(RiverFacet.class, facet);
    }

    @Override
    public String getConfigurationName() {
        return "Rivers";
    }

    @Override
    public Component getConfiguration() {
        return configuration;
    }

    @Override
    public void setConfiguration(Component configuration) {
        this.configuration = (Configuration) configuration;
        // Recreate noise with the new zoom
        setSeed(seed);
    }

    private static class Configuration implements Component {
        @Range(label = "River width", min = 1, max = 64f, increment = 1f, precision = 0, description = "Average river width (approximate)")
        public float riverWidth = 10;

        @Range(label = "River density", min = 0, max = 4f, increment = 0.1f, precision = 1)
        public float riverDensity = 0.5f;

        @Range(label = "River depth", min = 0, max = 64f, increment = 1f, precision = 0, description = "Maximum river Depth")
        public float maxDepth = 16;
    }
}
