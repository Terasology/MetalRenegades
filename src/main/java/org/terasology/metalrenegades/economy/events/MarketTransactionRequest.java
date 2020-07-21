package org.terasology.metalrenegades.economy.events;

import org.terasology.entitySystem.event.Event;
import org.terasology.metalrenegades.economy.ui.MarketItem;
import org.terasology.network.ServerEvent;

/**
 * A client-to-server event which requests the transaction of a particular item.
 */
@ServerEvent
public class MarketTransactionRequest implements Event {

    /**
     * The item to be bought/sold.
     */
    public MarketItem item;

    /**
     * The type of transaction to take place.
     */
    public TransactionType type;

    public MarketTransactionRequest(MarketItem item, TransactionType type) {
        this.item = item;
        this.type = type;
    }

    public MarketTransactionRequest() {

    }

}
