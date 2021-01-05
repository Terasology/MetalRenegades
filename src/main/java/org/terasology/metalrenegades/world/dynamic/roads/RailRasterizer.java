// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.metalrenegades.world.dynamic.roads;

import org.joml.Vector3i;
import org.terasology.cities.raster.RasterTarget;
import org.terasology.commonworld.heightmap.HeightMap;
import org.terasology.dynamicCities.parcels.RoadParcel;
import org.terasology.dynamicCities.rasterizer.RoadRasterizer;
import org.terasology.dynamicCities.roads.RoadSegment;
import org.terasology.math.JomlUtil;
import org.terasology.math.Side;
import org.terasology.math.geom.ImmutableVector2f;
import org.terasology.math.geom.Vector2i;
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
        ImmutableVector2f direction = roadSegment.getRoadDirection();

        Vector2i i = new Vector2i(roadSegment.start);
        i.add(new Vector2i(direction.scale(RoadParcel.OVERLAP)));

        do {
            Vector3i pos = new Vector3i(i.getX(), heightMap.apply(i) + 1, i.getY());
            placeRail(rasterTarget, pos);
            i.addX(sgn(direction.x())); // increment X to get to the next horizontal block
        } while (roadSegment.rect.contains(i) && direction.x() != 0f);

        i.subX(sgn(direction.x())); // decrement to get back into the rect
        i.addY(sgn(direction.y())); // increment Y now

        do {
            Vector3i pos = new Vector3i(i.getX(), heightMap.apply(i) + 1, i.getY());
            placeRail(rasterTarget, pos);
            i.addY(sgn(direction.y()));
        } while (roadSegment.rect.contains(i) && direction.y() != 0f);
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
        target.setBlock(JomlUtil.from(pos), RailBlockType.RAIL, connections);
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
