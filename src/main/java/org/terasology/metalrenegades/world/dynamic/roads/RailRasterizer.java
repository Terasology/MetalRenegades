// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.metalrenegades.world.dynamic.roads;

import org.joml.RoundingMode;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector2i;
import org.joml.Vector3i;
import org.terasology.cities.raster.RasterTarget;
import org.terasology.commonworld.heightmap.HeightMap;
import org.terasology.dynamicCities.parcels.RoadParcel;
import org.terasology.dynamicCities.rasterizer.RoadRasterizer;
import org.terasology.dynamicCities.roads.RoadSegment;
import org.terasology.math.JomlUtil;
import org.terasology.math.Side;
import org.terasology.minecarts.blocks.RailBlockFamily;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.family.BlockFamily;

import java.util.HashSet;
import java.util.Set;

/**
 * The rasterizer which calculates the path of the rail and places blocks accordingly.
 */
public class RailRasterizer extends RoadRasterizer {
    private WorldProvider worldProvider;

    public RailRasterizer(WorldProvider worldProvider) {
        this.worldProvider = worldProvider;
    }

    @Override
    public void raster(RasterTarget rasterTarget, RoadSegment roadSegment, HeightMap heightMap) {

        // Draw rails from start to end
        Vector2fc direction = JomlUtil.from(roadSegment.getRoadDirection());

        Vector2i i = new Vector2i(JomlUtil.from(roadSegment.start));
        i.add(new Vector2i(direction.mul(RoadParcel.OVERLAP, new Vector2f()), RoundingMode.FLOOR));

        do {
            Vector3i pos = new Vector3i(i.x(), heightMap.apply(JomlUtil.from(i)) + 1, i.y());
            placeRail(rasterTarget, pos);
            i.add(sgn(direction.x()), 0); // increment X to get to the next horizontal block
        } while (roadSegment.rect.contains(JomlUtil.from(i)) && direction.x() != 0f);

        i.sub(sgn(direction.x()), 0); // decrement to get back into the rect
        i.add(0, sgn(direction.y())); // increment Y now

        do {
            Vector3i pos = new Vector3i(i.x(), heightMap.apply(JomlUtil.from(i)) + 1, i.y());
            placeRail(rasterTarget, pos);
            i.add(0, sgn(direction.y()));
        } while (roadSegment.rect.contains(JomlUtil.from(i)) && direction.y() != 0f);
    }

    /**
     * Check for connections and place the appropriate rail block
     * @param target RasterTarget being processed
     * @param pos    Position to place the block
     */
    private void placeRail(RasterTarget target, Vector3i pos) {
        Set<Side> connections = new HashSet<>();
        for (Side side : Side.getAllSides()) {
            BlockFamily family = worldProvider.getBlock(side.getAdjacentPos(pos, new Vector3i())).getBlockFamily();
            if (family instanceof RailBlockFamily) {
                connections.add(side);
            }
        }
        target.setBlock(pos, RailBlockType.RAIL, connections);
    }

    /**
     * A function which casts the signum into an int before returning
     * @param a value to be evaluated
     * @return sign casted into an int
     */
    private int sgn(float a) {
        return (int) Math.signum(a);
    }
}
