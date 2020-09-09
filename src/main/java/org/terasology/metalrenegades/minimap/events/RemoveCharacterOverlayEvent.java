// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.metalrenegades.minimap.events;

import org.terasology.engine.entitySystem.event.Event;
import org.terasology.engine.network.BroadcastEvent;

/**
 * Event to be used to remove the character overlay once the citizen dies.
 */
@BroadcastEvent
public class RemoveCharacterOverlayEvent implements Event {
    public RemoveCharacterOverlayEvent() {

    }
}