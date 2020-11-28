// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.metalrenegades.minimap.events;

import org.terasology.entitySystem.event.Event;
import org.terasology.network.BroadcastEvent;

/**
 * Event to be used to add the character overlay to the minimap when the citizen is spawned.
 */
@BroadcastEvent
public class AddCharacterToOverlayEvent implements Event {
    public AddCharacterToOverlayEvent() {

    }
}
