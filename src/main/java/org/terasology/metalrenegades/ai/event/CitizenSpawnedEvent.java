// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.metalrenegades.ai.event;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.Event;
import org.terasology.protobuf.EntityData;

/**
 * Fired when a new citizen character is spawned by {@link org.terasology.metalrenegades.ai.system.CitizenSpawnSystem}.
 */
public class CitizenSpawnedEvent implements Event {


    // Stores the settlement to which the Citizen belongs to
    private EntityRef settlement;

    public CitizenSpawnedEvent(EntityRef settlement) {
        this.settlement = settlement;
    }

    public EntityRef getSettlement() {
        return this.settlement;
    }

}
