// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.metalrenegades.quests;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.Event;

/**
 * Adds the beacon overlay to the minimap
 */
public class AddBeaconOverlayEvent implements Event {
    public EntityRef beaconEntity;

    public AddBeaconOverlayEvent(EntityRef beaconEntity) {
        this.beaconEntity = beaconEntity;
    }

    public AddBeaconOverlayEvent() {
    }
}
