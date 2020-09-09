// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.metalrenegades.world.dynamic.discoverables;

import org.terasology.engine.utilities.procedural.Noise;
import org.terasology.engine.utilities.procedural.WhiteNoise;
import org.terasology.engine.world.generation.Border3D;
import org.terasology.engine.world.generation.Facet;
import org.terasology.engine.world.generation.FacetBorder;
import org.terasology.engine.world.generation.FacetProvider;
import org.terasology.engine.world.generation.GeneratingRegion;
import org.terasology.engine.world.generation.Produces;
import org.terasology.engine.world.generation.Requires;
import org.terasology.engine.world.generation.facets.SurfaceHeightFacet;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.Rect2i;

/**
 * Places chests into {@link DiscoverablesFacet} across the surface of the game world
 */
@Produces(DiscoverablesFacet.class)
@Requires(@Facet(value = SurfaceHeightFacet.class, border = @FacetBorder(bottom = 10, top = 10, sides = 10)))
public class DiscoverablesProvider implements FacetProvider {

    /**
     * The probability out of one that a chest will be placed on any particular surface block.
     */
    public static final float CHEST_PROBABILITY = 0.00001f;

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
        Border3D border = region.getBorderForFacet(DiscoverablesFacet.class).extendBy(10, 10, 10);

        SurfaceHeightFacet surfaceHeightFacet = region.getRegionFacet(SurfaceHeightFacet.class);
        DiscoverablesFacet facet = new DiscoverablesFacet(region.getRegion(), border);

        Rect2i worldRegion = surfaceHeightFacet.getWorldRegion();

        for (int wz = worldRegion.minY(); wz <= worldRegion.maxY(); wz++) {
            for (int wx = worldRegion.minX(); wx <= worldRegion.maxX(); wx++) {
                int surfaceHeight = TeraMath.floorToInt(surfaceHeightFacet.getWorld(wx, wz)) + 1;

                if (surfaceHeight >= facet.getWorldRegion().minY() &&
                        surfaceHeight <= facet.getWorldRegion().maxY() &&
                        facet.getWorldRegion().encompasses(wx, surfaceHeight, wz)) {
                    if (noise.noise(wx, wz) < (CHEST_PROBABILITY * 2) - 1) {
                        DiscoverableLocation.Type[] typeList = DiscoverableLocation.Type.values();
                        int typeIndex = (int) (Math.abs(noise.noise(wx, surfaceHeight, wz)) * typeList.length);
                        if (typeIndex > typeList.length) { // incredibly rare, but still possible with the noise range
                            typeIndex = 0;
                        }

                        DiscoverableLocation.Type type = typeList[typeIndex];
                        facet.setWorld(wx, surfaceHeight, wz, new DiscoverableLocation(type));
                    }
                }
            }
        }

        region.setRegionFacet(DiscoverablesFacet.class, facet);
    }
}
