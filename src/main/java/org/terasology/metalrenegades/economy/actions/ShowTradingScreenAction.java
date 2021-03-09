// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.metalrenegades.economy.actions;

import org.terasology.dialogs.action.PlayerAction;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.metalrenegades.economy.events.TradeScreenRequestEvent;

/**
 * Calls an event which would bring up the trading UI
 */
public class ShowTradingScreenAction implements PlayerAction {

    public ShowTradingScreenAction() {
    }

    @Override
    public void execute(EntityRef charEntity, EntityRef talkTo) {
        talkTo.send(new TradeScreenRequestEvent());
    }
}
