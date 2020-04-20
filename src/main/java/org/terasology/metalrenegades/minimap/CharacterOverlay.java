/*
 * Copyright 2020 MovingBlocks
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
package org.terasology.metalrenegades.minimap;


import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.geom.Rect2f;
import org.terasology.math.geom.Rect2fTransformer;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2f;
import org.terasology.metalrenegades.ai.component.CitizenComponent;
import org.terasology.minimap.overlays.MinimapOverlay;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.nui.Canvas;
import org.terasology.utilities.Assets;

import java.util.*;

/**
 * This class is used to add character overlays to the minimap based on the citizen type
 * and position
 */


public class CharacterOverlay implements MinimapOverlay {
    private static final float ICON_SIZE = 16f;

    private Vector2f iconSize = new Vector2f(ICON_SIZE, ICON_SIZE);

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
    }

    @Override
    public void render(Canvas canvas, Rect2f worldRect) {

        Collection<EntityRef> citizens = this.Citizens;

        Rect2f screenRect = Rect2f.createFromMinAndSize(
                new Vector2f(canvas.getRegion().minX(), canvas.getRegion().minY()),
                new Vector2f(canvas.getRegion().maxX(), canvas.getRegion().maxY())
        );

        Rect2fTransformer transformer = new Rect2fTransformer(worldRect, screenRect);


        for (EntityRef CitizenEntity : citizens) {
            if (!CitizenEntity.hasComponent(CitizenComponent.class)) {
                logger.error("No Citizen found!");
                continue;
            }


            LocationComponent locationComponent = CitizenEntity.getComponent(LocationComponent.class);
            if (locationComponent == null) {
                logger.error("Cannot find location component for Citizen: ");
                return;
            }

            Vector2f location = new Vector2f(locationComponent.getLocalPosition().x(), locationComponent.getLocalPosition().z());
            Vector2f mapPoint = new Vector2f(
                    transformer.applyX(location.x),
                    transformer.applyY(location.y)
            );
            Vector2f iconCenter = new Vector2f(mapPoint.x - iconSize.x / 2, mapPoint.y - iconSize.y / 2);

            if (isInside(iconCenter, screenRect)) {
                Rect2i region = Rect2i.createFromMinAndSize((int) iconCenter.x, (int) iconCenter.y, (int) iconSize.x, (int) iconSize.y);
                String citizenType = CitizenEntity.getParentPrefab().getName();
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
    private boolean isInside(Vector2f point, Rect2f box) {

        Rect2f iconRegion = Rect2f.createFromMinAndSize(point, iconSize);
        return box.contains(iconRegion);

    }

    /**
     * This function adds the citizen to the Citizens list
     *
     * @param entityRef citizen to be added
     */

    public void AddCitizen(EntityRef entityRef) {
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
