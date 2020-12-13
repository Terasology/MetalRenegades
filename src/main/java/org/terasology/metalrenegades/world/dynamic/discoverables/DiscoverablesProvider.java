// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.metalrenegades.world.dynamic.discoverables;

import org.terasology.math.Region3i;
import org.terasology.utilities.procedural.Noise;
import org.terasology.utilities.procedural.WhiteNoise;
import org.terasology.world.block.BlockRegion;
import org.terasology.world.generation.Border3D;
import org.terasology.world.generation.Facet;
import org.terasology.world.generation.FacetBorder;
import org.terasology.world.generation.FacetProvider;
import org.terasology.world.generation.GeneratingRegion;
import org.terasology.world.generation.Produces;
import org.terasology.world.generation.Requires;
import org.terasology.world.generation.facets.SurfacesFacet;

/**
 * Places chests into {@link DiscoverablesFacet} across the surface of the game world
 */
@Produces(DiscoverablesFacet.class)
@Requires(@Facet(value = SurfacesFacet.class, border = @FacetBorder(bottom = 10, top = 10, sides = 10)))
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

        SurfacesFacet surfacesFacet = region.getRegionFacet(SurfacesFacet.class);
        DiscoverablesFacet facet = new DiscoverablesFacet(region.getRegion(), border);

        BlockRegion worldRegion = surfacesFacet.getWorldRegion();

        for (int wx = worldRegion.getMinX(); wx <= worldRegion.getMaxX(); wx++) {
            for (int wz = worldRegion.getMinZ(); wz <= worldRegion.getMaxZ(); wz++) {
                for (int surfaceHeight : surfacesFacet.getWorldColumn(wx, wz)) {
                    if (facet.getWorldRegion().containsBlock(wx, surfaceHeight + 1, wz)) {
                        if (noise.noise(wx, wz) < (CHEST_PROBABILITY * 2) - 1) {
                            DiscoverableLocation.Type[] typeList = DiscoverableLocation.Type.values();
                            int typeIndex = (int) (Math.abs(noise.noise(wx, surfaceHeight, wz)) * typeList.length);
                            if (typeIndex > typeList.length) { // incredibly rare, but still possible with the noise range
                                typeIndex = 0;
                            }

                            DiscoverableLocation.Type type = typeList[typeIndex];
                            facet.setWorld(wx, surfaceHeight + 1, wz, new DiscoverableLocation(type));
                        }
                    }
                }
            }
        }

        region.setRegionFacet(DiscoverablesFacet.class, facet);
    }
}
