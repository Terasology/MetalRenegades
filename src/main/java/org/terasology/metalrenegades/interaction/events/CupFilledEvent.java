// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.metalrenegades.interaction.events;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.Event;

/**
 * Fires when a cup item is filled from a well.
 */
public class CupFilledEvent implements Event {

    /**
     * The entity gathering the water. Assumed to be a player entity.
     */
    private final EntityRef gatheringCharacter;

    /**
     * The new cup item given to the player upon refilling.
     */
    private final EntityRef cupItem;

    public CupFilledEvent(EntityRef gatheringCharacter, EntityRef cupItem) {
        this.gatheringCharacter = gatheringCharacter;
        this.cupItem = cupItem;
    }

    public EntityRef getGatheringCharacter() {
        return gatheringCharacter;
    }

    public EntityRef getCupItem() {
        return cupItem;
    }

}
