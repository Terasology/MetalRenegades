// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.metalrenegades.minimap.events;

import org.terasology.entitySystem.event.Event;
import org.terasology.network.BroadcastEvent;

/**
 * Event to be used to remove the character overlay once the citizen dies.
 */
@BroadcastEvent
public class RemoveCharacterFromOverlayEvent implements Event {
    public RemoveCharacterFromOverlayEvent() {

    }
}
