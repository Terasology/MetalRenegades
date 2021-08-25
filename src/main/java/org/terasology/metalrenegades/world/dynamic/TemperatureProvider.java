// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.metalrenegades.world.dynamic;

import org.joml.Vector2f;
import org.joml.Vector2ic;
import org.terasology.engine.utilities.procedural.BrownianNoise;
import org.terasology.engine.utilities.procedural.PerlinNoise;
import org.terasology.engine.utilities.procedural.SubSampledNoise;
import org.terasology.engine.world.generation.Border3D;
import org.terasology.engine.world.generation.GeneratingRegion;
import org.terasology.engine.world.generation.Produces;
import org.terasology.engine.world.generation.ScalableFacetProvider;
import org.terasology.engine.world.generation.facets.SurfaceTemperatureFacet;

@Produces(SurfaceTemperatureFacet.class)
public class TemperatureProvider implements ScalableFacetProvider {
    private  static final int SAMPLE_RATE = 4;

    private SubSampledNoise noise;

    @Override
    public void setSeed(long seed) {
        noise = new SubSampledNoise(new BrownianNoise(new PerlinNoise(seed + 5), 8), new Vector2f(0.0005f, 0.0005f), SAMPLE_RATE);
    }

    @Override
    public void process(GeneratingRegion region, float scale) {
        Border3D border = region.getBorderForFacet(SurfaceTemperatureFacet.class);
        SurfaceTemperatureFacet facet = new SurfaceTemperatureFacet(region.getRegion(), border);

        // TODO: Set temperature
        for (Vector2ic position: facet.getWorldArea()) {
            float temp = 0.6f;
            facet.setWorld(position, temp);
        }

        region.setRegionFacet(SurfaceTemperatureFacet.class, facet);
    }
}
