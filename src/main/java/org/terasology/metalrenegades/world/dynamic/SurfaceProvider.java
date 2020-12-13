// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.metalrenegades.world.dynamic;

import org.joml.Vector2f;
import org.terasology.math.geom.BaseVector2i;
import org.terasology.math.geom.Rect2i;
import org.terasology.utilities.procedural.Noise;
import org.terasology.utilities.procedural.SimplexNoise;
import org.terasology.utilities.procedural.SubSampledNoise;
import org.terasology.world.generation.Border3D;
import org.terasology.world.generation.FacetProvider;
import org.terasology.world.generation.GeneratingRegion;
import org.terasology.world.generation.Produces;
import org.terasology.world.generation.facets.ElevationFacet;

@Produces(ElevationFacet.class)
public class SurfaceProvider implements FacetProvider {
    private Noise surfaceNoise;

    @Override
    public void setSeed(long seed) {
        surfaceNoise = new SubSampledNoise(new SimplexNoise(seed), new Vector2f(0.01f, 0.01f), 1);
    }

    @Override
    public void process(GeneratingRegion region) {
        Border3D border = region.getBorderForFacet(ElevationFacet.class);
        ElevationFacet facet = new ElevationFacet(region.getRegion(), border);

        Rect2i processRegion = facet.getWorldRegion();
        for (BaseVector2i position: processRegion.contents()) {
            facet.setWorld(position, surfaceNoise.noise(position.x(), position.y()) * 3 + 10);
        }

        region.setRegionFacet(ElevationFacet.class, facet);
    }

}
