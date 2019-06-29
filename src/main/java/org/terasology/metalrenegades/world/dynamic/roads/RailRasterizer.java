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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.cities.DefaultBlockType;
import org.terasology.cities.raster.RasterTarget;
import org.terasology.commonworld.heightmap.HeightMap;
import org.terasology.dynamicCities.parcels.RoadParcel;
import org.terasology.dynamicCities.rasterizer.RoadRasterizer;
import org.terasology.dynamicCities.roads.RoadSegment;
import org.terasology.math.Region3i;
import org.terasology.math.Side;
import org.terasology.math.geom.BaseVector2i;
import org.terasology.math.geom.ImmutableVector2f;
import org.terasology.math.geom.Vector2i;
import org.terasology.math.geom.Vector3i;
import org.terasology.minecarts.blocks.RailBlockFamily;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.family.BlockFamily;

import java.util.HashSet;
import java.util.Set;

public class RailRasterizer extends RoadRasterizer {
    private WorldProvider worldProvider;

    private Logger logger = LoggerFactory.getLogger(RailRasterizer.class);

    public RailRasterizer(WorldProvider worldProvider) {
        this.worldProvider = worldProvider;
    }

    @Override
    public void raster(RasterTarget rasterTarget, RoadSegment roadSegment, HeightMap heightMap) {
        int upperHeight = 255;  // Height to which the region above the segment would be cleared

        // Clean the region above the rect
        Vector2i rectMin = roadSegment.rect.min();
        Region3i upper = Region3i.createFromMinAndSize(
                new Vector3i(rectMin.x(), heightMap.apply(rectMin) + 1, rectMin.y()),
                new Vector3i(roadSegment.rect.sizeX(), upperHeight, roadSegment.rect.sizeY())
        );

        for (Vector3i pos : upper) {
            rasterTarget.setBlock(pos, DefaultBlockType.AIR);
        }

        for (BaseVector2i pos : roadSegment.rect.contents()) {
            logger.info("Drawing dirt block at {}...", pos);
            rasterTarget.setBlock(new Vector3i(pos.x(), heightMap.apply(pos), pos.y()), RailBlockType.BASE);
        }

        // Draw rails from start to end
        ImmutableVector2f direction = roadSegment.getRoadDirection();

        Vector2i i = new Vector2i(roadSegment.start);
        i.add(new Vector2i(direction.scale(RoadParcel.OVERLAP)));

        do {
            Vector3i pos = new Vector3i(i.getX(), heightMap.apply(i) + 1, i.getY());
            placeRail(rasterTarget, pos);
            i.addX(sign(direction.x())); // increment X to get to the next horizontal block
        } while (roadSegment.rect.contains(i) && direction.x() != 0f);

        i.subX(sign(direction.x())); // decrement to get back into the rect
        i.addY(sign(direction.y())); // increment Y now

        do {
            Vector3i pos = new Vector3i(i.getX(), heightMap.apply(i) + 1, i.getY());
            placeRail(rasterTarget, pos);
            i.addY(sign(direction.y()));
        } while (roadSegment.rect.contains(i) && direction.y() != 0f);
    }

    private void placeRail(RasterTarget target, Vector3i pos) {
        // this function would check for connections and place a rail at the specified pos

        Set<Side> connections = new HashSet<>();
        for (Side side : Side.getAllSides()) {
            BlockFamily family = worldProvider.getBlock(side.getAdjacentPos(pos)).getBlockFamily();
            if (family instanceof RailBlockFamily) {
                connections.add(side);
            }
        }

        logger.info("Placing rail block at {} with {} connections", pos, connections.size());

        target.setBlock(pos, RailBlockType.RAIL, connections);
    }

    private int sign(float a) {
        if (a > 0) {
            return 1;
        } else if (a == 0) {
            return 0;
        } else {
            return -1;
        }
    }
}
