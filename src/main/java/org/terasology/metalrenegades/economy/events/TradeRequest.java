package org.terasology.metalrenegades.economy.events;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.Event;
import org.terasology.metalrenegades.economy.ui.MarketItem;
import org.terasology.network.ServerEvent;

@ServerEvent
public class TradeRequest implements Event {

    public MarketItem pItem;
    public MarketItem cItem;

    public EntityRef target;

    public TradeRequest(EntityRef target, MarketItem pItem, MarketItem cItem) {
        this.target = target;
        this.pItem = pItem;
        this.cItem = cItem;
    }

    public TradeRequest() { }

}
