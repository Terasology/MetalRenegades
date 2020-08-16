// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.metalrenegades.world.dynamic.discoverables;

import org.terasology.math.TeraMath;
import org.terasology.math.geom.Rect2i;
import org.terasology.utilities.procedural.Noise;
import org.terasology.utilities.procedural.WhiteNoise;
import org.terasology.world.generation.*;
import org.terasology.world.generation.facets.SurfaceHeightFacet;

/**
 * Places chests into {@link DiscoverablesFacet} across the surface of the game world
 */
@Produces(DiscoverablesFacet.class)
@Requires(@Facet(value = SurfaceHeightFacet.class, border = @FacetBorder(sides=1, top=1)))
public class DiscoverablesProvider implements FacetProvider {

    /**
     * The probability out of one that a chest will be placed on any particular surface block.
     */
    public static final float CHEST_PROBABILITY = 0.00005f;

    /**
     * A noise provider that determines the positions of discoverables.
     */
    private Noise noise;

    @Override
    public void setSeed(long seed) {
        noise = new WhiteNoise(seed);
    }

    @Override
    public void process(GeneratingRegion region) {
        Border3D border = region.getBorderForFacet(DiscoverablesFacet.class).extendBy(1, 0, 1);

        SurfaceHeightFacet surfaceHeightFacet = region.getRegionFacet(SurfaceHeightFacet.class);
        DiscoverablesFacet facet = new DiscoverablesFacet(region.getRegion(), border);

        Rect2i worldRegion = surfaceHeightFacet.getWorldRegion();

        for (int wz = worldRegion.minY(); wz <= worldRegion.maxY(); wz++) {
            for (int wx = worldRegion.minX(); wx <= worldRegion.maxX(); wx++) {
                int surfaceHeight = TeraMath.floorToInt(surfaceHeightFacet.getWorld(wx, wz));

                if (surfaceHeight >= facet.getWorldRegion().minY() &&
                    surfaceHeight <= facet.getWorldRegion().maxY()) {
                    if (noise.noise(wx, wz) < (CHEST_PROBABILITY * 2) - 1) {
                        facet.setWorld(wx, surfaceHeight + 1, wz, new DiscoverablesChest());
                    }
                }
            }
        }

        region.setRegionFacet(DiscoverablesFacet.class, facet);
    }
}
