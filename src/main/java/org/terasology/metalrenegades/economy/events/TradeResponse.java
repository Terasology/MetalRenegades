// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.metalrenegades.economy.events;

import org.terasology.entitySystem.event.Event;
import org.terasology.network.OwnerEvent;

/**
 * A server-client response describing if the trade was successful or not.
 */
@OwnerEvent
public class TradeResponse implements Event {

    /**
     * True if this trade was successful, false otherwise.
     */
    public boolean successful;

    /**
     * The response message from the citizen, with reasoning behind accepting/rejecting a particular trade. Displayed
     * on the trade UI.
     */
    public String message;

    public TradeResponse() {}

    public TradeResponse(boolean success, String message) {
        this.successful = success;
        this.message = message;
    }

}
