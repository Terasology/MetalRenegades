// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.metalrenegades.world.dynamic;

import org.terasology.engine.utilities.procedural.BrownianNoise;
import org.terasology.engine.utilities.procedural.PerlinNoise;
import org.terasology.engine.utilities.procedural.SubSampledNoise;
import org.terasology.engine.world.generation.Border3D;
import org.terasology.engine.world.generation.FacetProvider;
import org.terasology.engine.world.generation.GeneratingRegion;
import org.terasology.engine.world.generation.Produces;
import org.terasology.engine.world.generation.facets.SurfaceTemperatureFacet;
import org.terasology.math.geom.BaseVector2i;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2f;

@Produces(SurfaceTemperatureFacet.class)
public class TemperatureProvider implements FacetProvider {
    private static final int SAMPLE_RATE = 4;

    private SubSampledNoise noise;
    private float[] temperatures;

    @Override
    public void setSeed(long seed) {
        noise = new SubSampledNoise(new BrownianNoise(new PerlinNoise(seed + 5), 8), new Vector2f(0.0005f, 0.0005f),
                SAMPLE_RATE);
    }

    @Override
    public void initialize() {

    }

    @Override
    public void process(GeneratingRegion region) {
        Border3D border = region.getBorderForFacet(SurfaceTemperatureFacet.class);
        SurfaceTemperatureFacet facet = new SurfaceTemperatureFacet(region.getRegion(), border);

        // TODO: Set temperature
        Rect2i processRegion = facet.getWorldRegion();
        for (BaseVector2i position : processRegion.contents()) {
            double temp = getRandomTemp(position);
            facet.setWorld(position, (float) temp);
        }

        region.setRegionFacet(SurfaceTemperatureFacet.class, facet);
    }

    private float generateWeight() {
        return 0;
    }

    private double getRandomTemp(BaseVector2i pos) {
//        float sumOfWeights = 10; // TODO
//        Random random = new Random();
////        float f = random.nextFloat() * sumOfWeights;
//        return Math.sqrt(random.nextFloat());
        return 0.6f;
    }
}
