// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.metalrenegades.economy.actions;

import org.terasology.dialogs.action.PlayerAction;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.metalrenegades.economy.events.MarketScreenRequestEvent;
import org.terasology.metalrenegades.economy.events.TransactionType;


/**
 * Fires up an event which brings up the market UI
 */
public class ShowMarketScreenAction implements PlayerAction {

    private long marketID;
    private TransactionType type;

    public ShowMarketScreenAction(long marketID) {
        this.marketID = marketID;
        this.type = TransactionType.BUYING;
    }

    public ShowMarketScreenAction(long marketID, TransactionType type) {
        this.marketID = marketID;
        this.type = type;
    }

    @Override
    public void execute(EntityRef charEntity, EntityRef talkTo) {
        talkTo.send(new MarketScreenRequestEvent(marketID, charEntity, type));
    }

}

