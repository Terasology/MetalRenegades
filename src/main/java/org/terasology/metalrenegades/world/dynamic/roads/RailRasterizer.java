/*
 * Copyright 2019 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.metalrenegades.world.dynamic.roads;

import org.terasology.cities.raster.RasterTarget;
import org.terasology.commonworld.heightmap.HeightMap;
import org.terasology.dynamicCities.parcels.RoadParcel;
import org.terasology.dynamicCities.rasterizer.RoadRasterizer;
import org.terasology.dynamicCities.roads.RoadSegment;
import org.terasology.math.Side;
import org.terasology.math.geom.ImmutableVector2f;
import org.terasology.math.geom.Vector2i;
import org.terasology.math.geom.Vector3i;
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
            BlockFamily family = worldProvider.getBlock(side.getAdjacentPos(pos)).getBlockFamily();
            if (family instanceof RailBlockFamily) {
                connections.add(side);
            }
        }
        target.setBlock(pos, RailBlockType.RAIL, connections);
    }

    /**
     * The signum function
     * @param a value to be evaluated
     * @return 1 if a > 0; 0 if a == 0; -1 if a < 0
     */
    private int sgn(float a) {
        if (a > 0) {
            return 1;
        } else if (a == 0) {
            return 0;
        } else {
            return -1;
        }
    }
}
