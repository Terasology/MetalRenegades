// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.metalrenegades.world.dynamic.discoverables;

import org.terasology.engine.utilities.procedural.Noise;
import org.terasology.engine.utilities.procedural.WhiteNoise;
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.engine.world.generation.Border3D;
import org.terasology.engine.world.generation.Facet;
import org.terasology.engine.world.generation.FacetBorder;
import org.terasology.engine.world.generation.FacetProvider;
import org.terasology.engine.world.generation.GeneratingRegion;
import org.terasology.engine.world.generation.Produces;
import org.terasology.engine.world.generation.Requires;
import org.terasology.engine.world.generation.facets.SeaLevelFacet;
import org.terasology.engine.world.generation.facets.SurfacesFacet;

/**
 * Places chests into {@link DiscoverablesFacet} across the surface of the game world
 */
@Produces(DiscoverablesFacet.class)
@Requires({
        @Facet(value = SurfacesFacet.class, border = @FacetBorder(bottom = 10, top = 10, sides = 10)),
        @Facet(SeaLevelFacet.class)
})
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

        SeaLevelFacet seaLevelFacet = region.getRegionFacet(SeaLevelFacet.class);
        int seaLevel = seaLevelFacet.getSeaLevel();
        SurfacesFacet surfacesFacet = region.getRegionFacet(SurfacesFacet.class);
        DiscoverablesFacet facet = new DiscoverablesFacet(region.getRegion(), border);

        BlockRegion worldRegion = surfacesFacet.getWorldRegion();

        for (int wx = worldRegion.minX(); wx <= worldRegion.maxX(); wx++) {
            for (int wz = worldRegion.minZ(); wz <= worldRegion.maxZ(); wz++) {
                for (int surfaceHeight : surfacesFacet.getWorldColumn(wx, wz)) {
                    if (surfaceHeight >= seaLevel && facet.getWorldRegion().contains(wx, surfaceHeight + 1, wz)) {
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
