// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.metalrenegades.interaction.events;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.Event;

/**
 * Fires when a player drinks directly from a well.
 */
public class WellDrinkEvent implements Event {

    /**
     * The entity drinking the water. Assumed to be a player entity.
     */
    private final EntityRef gatheringCharacter;

    public WellDrinkEvent(EntityRef gatheringCharacter) {
        this.gatheringCharacter = gatheringCharacter;
    }

    public EntityRef getGatheringCharacter() {
        return gatheringCharacter;
    }

}
