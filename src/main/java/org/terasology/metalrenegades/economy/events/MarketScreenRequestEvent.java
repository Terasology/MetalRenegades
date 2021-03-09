// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.metalrenegades.economy.events;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.Event;

/**
 * Fired when the market screen needs to be shown
 */
public class MarketScreenRequestEvent implements Event{
    public long market;
    public EntityRef talkTo;
    public TransactionType type;

    public MarketScreenRequestEvent(long market, EntityRef talkTo, TransactionType type) {
        this.market = market;
        this.talkTo = talkTo;
        this.type = type;
    }

    public MarketScreenRequestEvent() {}
}
