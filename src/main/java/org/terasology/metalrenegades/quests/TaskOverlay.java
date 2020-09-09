// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.metalrenegades.quests;

import org.joml.Rectanglei;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.math.JomlUtil;
import org.terasology.engine.rendering.assets.texture.Texture;
import org.terasology.engine.utilities.Assets;
import org.terasology.minimap.overlays.MinimapOverlay;
import org.terasology.nui.Canvas;
import org.terasology.nui.util.RectUtility;

import java.util.Optional;

/**
 * A minimap overlay to mark the home beacon for the meat quest
 */
public class TaskOverlay implements MinimapOverlay {

    private final int ICON_SIZE = 24;
    private final EntityRef beaconEntity;

    public TaskOverlay(EntityRef beaconEntity) {
        this.beaconEntity = beaconEntity;
    }

    @Override
    public void render(Canvas canvas, Rectanglei worldRect) {

        Vector3f localPosition = JomlUtil.from(beaconEntity.getComponent(LocationComponent.class).getWorldPosition());
        Vector2i mapPoint = RectUtility.map(worldRect, canvas.getRegion(), new Vector2i((int) localPosition.x,
                (int) localPosition.y), new Vector2i());
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
                x = box.maxX - ICON_SIZE;
            } else if (iconRegion.minX <= box.minX) {
                x = box.minX;
            } else {
                x = point.x;
            }

            if (iconRegion.maxY >= box.maxY) {
                y = box.maxY - ICON_SIZE;
            } else if (iconRegion.minY <= box.minY) {
                y = box.minY;
            } else {
                y = point.y;
            }
        }
        return new Vector2i(x, y);
    }
}
