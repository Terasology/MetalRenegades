// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.metalrenegades.minimap;


import org.joml.Rectanglei;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.location.LocationComponent;
import org.terasology.minimap.overlays.MinimapOverlay;
import org.terasology.nui.Canvas;
import org.terasology.nui.util.RectUtility;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.utilities.Assets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * This class is used to add character overlays to the minimap based on the citizen type
 * and position
 */


public class CharacterOverlay implements MinimapOverlay {
    private static final int ICON_SIZE = 16;

    private Vector2i iconSize = new Vector2i(ICON_SIZE, ICON_SIZE);

    private Logger logger = LoggerFactory.getLogger(CharacterOverlay.class);

    private ArrayList<EntityRef> Citizens;

    private final Map<String, Optional> map = new HashMap<String, Optional>();


    /**
     * This constructor sets creates the list of citizens
     */
    public CharacterOverlay() {
        this.Citizens = new ArrayList<EntityRef>();
        this.map.put("MetalRenegades:marketCitizen", Assets.getTexture("MetalRenegades:marketGooey"));
        this.map.put("MetalRenegades:badCitizen", Assets.getTexture("MetalRenegades:badCitizen"));
        this.map.put("MetalRenegades:goodCitizen", Assets.getTexture("MetalRenegades:goodCitizen"));
        this.map.put("MetalRenegades:gooeyCitizen", Assets.getTexture("MetalRenegades:gooeyCitizen"));
        this.map.put("MetalRenegades:neutralGooey", Assets.getTexture("MetalRenegades:neutralGooey"));
        this.map.put("MetalRenegades:nocturnalGooey", Assets.getTexture("MetalRenegades:nocturnalGooey"));
        this.map.put("MetalRenegades:scaredGooey", Assets.getTexture("MetalRenegades:scaredGooey"));
        this.map.put("MetalRenegades:angryGooey", Assets.getTexture("MetalRenegades:angryGooey"));
        this.map.put("MetalRenegades:friendlyGooey", Assets.getTexture("MetalRenegades:friendlyGooey"));
        this.map.put("MetalRenegades:enemyGooey", Assets.getTexture("MetalRenegades:enemyIcon"));
    }

    @Override
    public void render(Canvas canvas, Rectanglei worldRect) {

        Collection<EntityRef> citizens = this.Citizens;

        for (EntityRef entity : citizens) {
            LocationComponent locationComponent = entity.getComponent(LocationComponent.class);
            if (locationComponent == null) {
                logger.error("Cannot find location component for Citizen: ");
                return;
            }

            Vector2f location = new Vector2f(locationComponent.getLocalPosition().x(), locationComponent.getLocalPosition().z());
            Vector2i mapPoint = RectUtility.map(worldRect, canvas.getRegion(), new Vector2i((int) location.x, (int) location.y), new Vector2i());
            Vector2i iconCenter = new Vector2i((int) (mapPoint.x - (iconSize.x / 2.0f)), (int) (mapPoint.y - (iconSize.y / 2.0f)));

            if (isInside(iconCenter, canvas.getRegion())) {
                Rectanglei region = RectUtility.createFromMinAndSize((int) iconCenter.x, (int) iconCenter.y, (int) iconSize.x, (int) iconSize.y);
                String citizenType = entity.getParentPrefab().getName();
                if (this.map.get(citizenType) != null) {
                    Optional<Texture> icon = this.map.get(citizenType);
                    if (icon.isPresent()) {
                        canvas.drawTexture(icon.get(), region);
                    } else {
                        logger.error("No icon found for citizen" + citizenType);
                    }
                }
            }
        }
    }

    /**
     * Checks if the citizen to be drawn lies inside the visible region in the minimap
     *
     * @param point: the coordinates of the point to be checked
     * @param box:   limits
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
        this.Citizens.add(entityRef);
    }

    /**
     * This function removes the citizen from the list
     *
     * @param entityRef citizen to be removed
     */

    public void removeCitizen(EntityRef entityRef) {
        this.Citizens.remove(entityRef);

    }


    @Override
    public int getZOrder() {
        return 2;
    }

}
