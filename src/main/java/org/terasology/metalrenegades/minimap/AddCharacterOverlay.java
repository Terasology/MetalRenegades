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


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.dynamicCities.minimap.DistrictOverlay;
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

import java.util.Optional;

public class AddCharacterOverlay implements MinimapOverlay {
    private static final float ICON_SIZE = 32f;

    private EntityRef CitizenEntity;
    private Vector2f iconSize = new Vector2f(ICON_SIZE, ICON_SIZE);

    private Logger logger = LoggerFactory.getLogger(DistrictOverlay.class);

    public AddCharacterOverlay(EntityRef entityRef) {
        this.CitizenEntity = entityRef;
    }

    @Override
    public void render(Canvas canvas, Rect2f worldRect) {
        if (!CitizenEntity.hasComponent(CitizenComponent.class)) {
            logger.error("No Citizen found!");
            return;
        }

        Rect2f screenRect = Rect2f.createFromMinAndSize(
                new Vector2f(canvas.getRegion().minX(), canvas.getRegion().minY()),
                new Vector2f(canvas.getRegion().maxX(), canvas.getRegion().maxY())
        );

        Rect2fTransformer transformer = new Rect2fTransformer(worldRect, screenRect);


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

        if (isInside(mapPoint, screenRect)) {
            Rect2i region = Rect2i.createFromMinAndSize((int) mapPoint.x, (int) mapPoint.y, (int) iconSize.x / 2, (int) iconSize.y / 2);
            String citizenType = CitizenEntity.getParentPrefab().getName();
            Optional<Texture> icon;
            if (citizenType.equals("MetalRenegades:marketCitizen")) {
                icon = Assets.getTexture("MetalRenegades:marketGooey");
            } else {

                icon = Assets.getTexture(citizenType);
            }
            if (icon.isPresent()) {
                canvas.drawTexture(icon.get(), region);
            } else {
                logger.error("No icon found for citizen" + citizenType);
            }
        }

    }

    /**
     * Checks if the citizen to be drawn lies inside the visible region in the minimap
     *
     * @param point: the coordinates of the point to be clamped
     * @param box:   limits
     * @return whether point is to be drawn or not
     */
    private boolean isInside(Vector2f point, Rect2f box) {

        Rect2f iconRegion = Rect2f.createFromMinAndSize(point, iconSize);
        return box.contains(iconRegion);

    }


    @Override
    public int getZOrder() {
        return 0;
    }

}
