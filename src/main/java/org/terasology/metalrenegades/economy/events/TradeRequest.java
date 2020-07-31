// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.metalrenegades.economy.events;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.Event;
import org.terasology.metalrenegades.economy.ui.MarketItem;
import org.terasology.network.ServerEvent;

/**
 * A client-server request to initiate a trade between a player and an NPC character.
 */
@ServerEvent
public class TradeRequest implements Event {

    /**
     * The item that the player wishes to trade.
     */
    public MarketItem pItem;

    /**
     * The item that the citizen would trade.
     */
    public MarketItem cItem;

    /**
     * The citizen that is trading with the player.
     */
    public EntityRef target;

    public TradeRequest(EntityRef target, MarketItem pItem, MarketItem cItem) {
        this.target = target;
        this.pItem = pItem;
        this.cItem = cItem;
    }

    public TradeRequest() { }

}
