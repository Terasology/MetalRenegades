// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.metalrenegades.economy.events;

import org.terasology.engine.entitySystem.event.Event;
import org.terasology.engine.network.ServerEvent;
import org.terasology.metalrenegades.economy.ui.MarketItem;

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
