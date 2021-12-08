// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.metalrenegades.minimap.events;

import org.terasology.engine.network.BroadcastEvent;
import org.terasology.gestalt.entitysystem.event.Event;

/**
 * Event to be used to remove the character overlay once the citizen dies.
 */
@BroadcastEvent
public class RemoveCharacterFromOverlayEvent implements Event {
    public RemoveCharacterFromOverlayEvent() {

    }
}
