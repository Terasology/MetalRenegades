// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.metalrenegades.minimap;

import org.joml.Vector2f;
import org.joml.Vector2i;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.joml.geom.Rectanglei;
import org.terasology.logic.location.LocationComponent;
import org.terasology.minimap.overlays.MinimapOverlay;
import org.terasology.nui.Canvas;
import org.terasology.nui.util.RectUtility;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.utilities.Assets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * This class is used to add character overlays to the minimap based on the citizen type and position
 */


public class CharacterOverlay implements MinimapOverlay {
    private static final Logger logger = LoggerFactory.getLogger(CharacterOverlay.class);
    private static final int ICON_SIZE = 16;

    private Vector2i iconSize = new Vector2i(ICON_SIZE, ICON_SIZE);
    private ArrayList<EntityRef> citizens;
    private final Map<String, Texture> overlays = new HashMap<>();


    /**
     * This constructor sets creates the list of citizens
     */
    public CharacterOverlay() {
        this.citizens = new ArrayList<EntityRef>();
        registerOverlay("MetalRenegades:marketCitizen", () -> Assets.getTexture("MetalRenegades:marketGooey"));
        registerOverlay("MetalRenegades:badCitizen", () -> Assets.getTexture("MetalRenegades:badCitizen"));
        registerOverlay("MetalRenegades:goodCitizen", () -> Assets.getTexture("MetalRenegades:goodCitizen"));
        registerOverlay("MetalRenegades:gooeyCitizen", () -> Assets.getTexture("MetalRenegades:gooeyCitizen"));
        registerOverlay("MetalRenegades:neutralGooey", () -> Assets.getTexture("MetalRenegades:neutralGooey"));
        registerOverlay("MetalRenegades:nocturnalGooey", () -> Assets.getTexture("MetalRenegades:nocturnalGooey"));
        registerOverlay("MetalRenegades:scaredGooey", () -> Assets.getTexture("MetalRenegades:scaredGooey"));
        registerOverlay("MetalRenegades:angryGooey", () -> Assets.getTexture("MetalRenegades:angryGooey"));
        registerOverlay("MetalRenegades:friendlyGooey", () -> Assets.getTexture("MetalRenegades:friendlyGooey"));
        registerOverlay("MetalRenegades:enemyGooey", () -> Assets.getTexture("MetalRenegades:enemyIcon"));
    }

    private void registerOverlay(final String citizenType, Supplier<Optional<Texture>> textureSupplier) {
        Optional<Texture> texture = textureSupplier.get();
        if (texture.isPresent()) {
            overlays.put(citizenType, texture.get());
        } else {
            logger.warn("No icon found for citizen '{}'", citizenType);
        }
    }

    @Override
    public void render(Canvas canvas, Rectanglei worldRect) {
        for (EntityRef entity : citizens) {
            LocationComponent locationComponent = entity.getComponent(LocationComponent.class);
            if (locationComponent == null) {
                logger.warn("Cannot render overlay for entity '{}' - missing LocationComponent", entity.getId());
                continue;
            }
            String citizenType = entity.getParentPrefab().getName();
            Optional<Texture> icon = Optional.ofNullable(overlays.get(citizenType));
            if (icon.isPresent()) {
                Vector2f location = new Vector2f(locationComponent.getLocalPosition().x(),
                        locationComponent.getLocalPosition().z());
                Vector2i mapPoint = RectUtility.map(worldRect, canvas.getRegion(), new Vector2i((int) location.x,
                        (int) location.y), new Vector2i());
                Vector2i iconCenter = new Vector2i((int) (mapPoint.x - (iconSize.x / 2.0f)),
                        (int) (mapPoint.y - (iconSize.y / 2.0f)));
                if (isInside(iconCenter, canvas.getRegion())) {
                    Rectanglei region = RectUtility.createFromMinAndSize(iconCenter.x, iconCenter.y, iconSize.x,
                            iconSize.y);
                    canvas.drawTexture(icon.get(), region);
                }
            }
        }
    }

    /**
     * Checks if the citizen to be drawn lies inside the visible region in the minimap
     *
     * @param point: the coordinates of the point to be checked
     * @param box: limits
     * @return whether point is to be drawn or not
     */
    private boolean isInside(Vector2i point, Rectanglei box) {
        Rectanglei iconRegion = RectUtility.createFromMinAndSize(point, iconSize);
        return box.containsRectangle(iconRegion);
    }

    /**
     * This function adds the citizen to the Citizens list
     *
     * @param entityRef citizen to be added
     */

    public void addCitizen(EntityRef entityRef) {
        this.citizens.add(entityRef);
    }

    /**
     * This function removes the citizen from the list
     *
     * @param entityRef citizen to be removed
     */

    public void removeCitizen(EntityRef entityRef) {
        this.citizens.remove(entityRef);

    }


    @Override
    public int getZOrder() {
        return 2;
    }

}
