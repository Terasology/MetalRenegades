// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.metalrenegades.minimap;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.registry.In;
import org.terasology.metalrenegades.minimap.events.AddCharacterToOverlayEvent;
import org.terasology.metalrenegades.minimap.events.RemoveCharacterFromOverlayEvent;
import org.terasology.metalrenegades.minimap.events.RemoveCharacterOverlayEvent;
import org.terasology.minimap.logic.MinimapSystem;

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
        characterOverlay.addCitizen(citizen);
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
