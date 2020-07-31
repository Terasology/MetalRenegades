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
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.players.MinimapSystem;
import org.terasology.metalrenegades.minimap.events.AddCharacterToOverlayEvent;
import org.terasology.metalrenegades.minimap.events.RemoveCharacterFromOverlayEvent;
import org.terasology.metalrenegades.minimap.events.RemoveCharacterOverlayEvent;
import org.terasology.registry.In;

/**
 * This system manages the events to add and remove character overlays
 */

@RegisterSystem(RegisterMode.CLIENT)
public class AddCharacterPositionOverlaySystem extends BaseComponentSystem {

    Logger logger = LoggerFactory.getLogger(AddCharacterPositionOverlaySystem.class);

    @In
    private MinimapSystem minimapSystem;

    @In
    private EntityManager entityManager;

    private CharacterOverlay characterOverlay;

    /**
     * Adds the Character Overlay to the map
     */
    @Override
    public void initialise() {
        characterOverlay = new CharacterOverlay();
        minimapSystem.addOverlay(characterOverlay);
    }

    /**
     * Adds every citizen that sends this event
     *
     * @param event
     * @param citizen
     */
    @ReceiveEvent
    public void onAddCharacterOverlayEvent(AddCharacterToOverlayEvent event, EntityRef citizen) {
        characterOverlay.AddCitizen(citizen);
    }

    /**
     * Removes CharacterOverlay from the map
     *
     * @param event
     * @param entityRef
     */
    @ReceiveEvent
    public void onRemoveCharacterOverlayFromEvent(RemoveCharacterFromOverlayEvent event, EntityRef entityRef) {
        logger.info("{} has been removed from the minimap ", entityRef.toString());
        characterOverlay.removeCitizen(entityRef);
    }

    /**
     * Removes CharacterOverlay from the map
     *
     * @param event
     * @param entityRef
     */
    @ReceiveEvent
    public void onRemoveCharacterOverlayEvent(RemoveCharacterOverlayEvent event, EntityRef entityRef) {
        minimapSystem.removeOverlay(characterOverlay);
    }
}
