/*
 * Copyright 2019 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.metalrenegades.economy.actions;

import org.terasology.dialogs.action.PlayerAction;
import org.terasology.dynamicCities.buildings.components.SettlementRefComponent;
import org.terasology.dynamicCities.settlements.components.MarketComponent;
import org.terasology.entitySystem.entity.EntityRef;
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

