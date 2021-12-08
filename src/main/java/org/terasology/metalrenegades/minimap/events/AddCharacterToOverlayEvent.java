// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.metalrenegades.minimap.events;

import org.terasology.engine.network.BroadcastEvent;
import org.terasology.gestalt.entitysystem.event.Event;

/**
 * Event to be used to add the character overlay to the minimap when the citizen is spawned.
 */
@BroadcastEvent
public class AddCharacterToOverlayEvent implements Event {
    public AddCharacterToOverlayEvent() {

    }
}
