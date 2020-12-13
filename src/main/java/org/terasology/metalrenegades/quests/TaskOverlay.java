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
package org.terasology.metalrenegades.quests;

import org.joml.primitives.Rectanglei;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.JomlUtil;
import org.terasology.minimap.overlays.MinimapOverlay;
import org.terasology.nui.Canvas;
import org.terasology.nui.util.RectUtility;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.utilities.Assets;

import java.util.Optional;

/**
 * A minimap overlay to mark the home beacon for the meat quest
 */
public class TaskOverlay implements MinimapOverlay {

    private final int ICON_SIZE = 24;
    private EntityRef beaconEntity;

    public TaskOverlay(EntityRef beaconEntity) {
        this.beaconEntity = beaconEntity;
    }

    @Override
    public void render(Canvas canvas, Rectanglei worldRect) {

        Vector3f localPosition = JomlUtil.from(beaconEntity.getComponent(LocationComponent.class).getWorldPosition());
        Vector2i mapPoint = RectUtility.map(worldRect, canvas.getRegion(), new Vector2i((int) localPosition.x, (int) localPosition.y), new Vector2i());
        Vector2i min = clamp(mapPoint, canvas.getRegion());

        Rectanglei region = RectUtility.createFromMinAndSize(min.x, min.y, ICON_SIZE, ICON_SIZE);
        Optional<Texture> icon = Assets.getTexture("MetalRenegades:beaconIcon");
        icon.ifPresent(texture -> canvas.drawTexture(texture, region));
    }

    @Override
    public int getZOrder() {
        return 0;
    }

    public EntityRef getBeaconEntity() {
        return beaconEntity;
    }

    /**
     * Constrains a point to a specified region. Works like a vector clamp.
     *
     * @param point: the coordinates of the point to be clamped
     * @param box: limits
     * @return new clamped coordinates of point
     */
    private Vector2i clamp(Vector2i point, Rectanglei box) {
        int x;
        int y;
        Rectanglei iconRegion = RectUtility.createFromMinAndSize(point.x, point.y, ICON_SIZE, ICON_SIZE);

        if (box.containsRectangle(iconRegion)) {
            return new Vector2i(point.x, point.y);
        } else {
            if (iconRegion.maxX >= box.maxX) {
                x = (int) box.maxX - ICON_SIZE;
            } else if (iconRegion.minX <= box.minX) {
                x = (int) box.minX;
            } else {
                x = point.x;
            }

            if (iconRegion.maxY >= box.maxY) {
                y = (int) box.maxY - ICON_SIZE;
            } else if (iconRegion.minY <= box.minY) {
                y = (int) box.minY;
            } else {
                y = point.y;
            }
        }
        return new Vector2i(x, y);
    }
}
