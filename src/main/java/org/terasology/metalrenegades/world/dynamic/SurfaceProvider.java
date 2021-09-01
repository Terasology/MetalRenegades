// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.metalrenegades.world.dynamic;

import org.joml.Vector2f;
import org.terasology.engine.utilities.procedural.SimplexNoise;
import org.terasology.engine.utilities.procedural.SubSampledNoise;
import org.terasology.engine.world.generation.Border3D;
import org.terasology.engine.world.generation.GeneratingRegion;
import org.terasology.engine.world.generation.Produces;
import org.terasology.engine.world.generation.ScalableFacetProvider;
import org.terasology.engine.world.generation.facets.ElevationFacet;

@Produces(ElevationFacet.class)
public class SurfaceProvider implements ScalableFacetProvider {
    private SubSampledNoise surfaceNoise;

    @Override
    public void setSeed(long seed) {
        surfaceNoise = new SubSampledNoise(new SimplexNoise(seed), new Vector2f(0.01f, 0.01f), 1);
    }

    @Override
    public void process(GeneratingRegion region, float scale) {
        Border3D border = region.getBorderForFacet(ElevationFacet.class);
        ElevationFacet facet = new ElevationFacet(region.getRegion(), border);

        float[] surfaceData = surfaceNoise.noise(facet.getWorldArea(), scale);
        float[] surfaceInternal = facet.getInternal();
        for (int i = 0; i < surfaceInternal.length; i++) {
            surfaceInternal[i] = surfaceData[i] * 3 + 10;
        }

        region.setRegionFacet(ElevationFacet.class, facet);
    }

}
