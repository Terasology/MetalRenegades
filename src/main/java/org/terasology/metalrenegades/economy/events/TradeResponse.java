package org.terasology.metalrenegades.economy.events;

import org.terasology.entitySystem.event.Event;
import org.terasology.network.OwnerEvent;

@OwnerEvent
public class TradeResponse implements Event {

    public boolean successful;
    public String message;

    public TradeResponse() {}

    public TradeResponse(boolean success, String message) {
        this.successful = success;
        this.message = message;
    }

}
