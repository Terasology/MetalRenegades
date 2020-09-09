// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.metalrenegades.quests;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.Event;

/**
 * Removes the beacon overlay from the minimap
 */
public class RemoveBeaconOverlayEvent implements Event {
    public EntityRef beaconEntity;

    public RemoveBeaconOverlayEvent(EntityRef beaconEntity) {
        this.beaconEntity = beaconEntity;
    }
}
