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
package org.terasology.metalrenegades.economy.ui;

import net.logstash.logback.encoder.org.apache.commons.lang.ArrayUtils;
import org.terasology.assets.ResourceUrn;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.characters.interactions.InteractionUtil;
import org.terasology.logic.inventory.InventoryComponent;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.metalrenegades.economy.events.MarketScreenRequestEvent;
import org.terasology.metalrenegades.economy.events.TradeScreenRequestEvent;
import org.terasology.metalrenegades.economy.events.TransactionType;
import org.terasology.protobuf.EntityData;
import org.terasology.registry.In;
import org.terasology.registry.Share;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.segmentedpaths.controllers.SegmentMapping;

import java.util.ArrayList;
import java.util.List;

@Share(TradingUISystem.class)
@RegisterSystem(RegisterMode.CLIENT)
public class TradingUISystem extends BaseComponentSystem {

    @In
    private NUIManager nuiManager;

    @In
    private InventoryManager inventoryManager;

    @In
    private LocalPlayer localPlayer;

    private TradingScreen tradingScreen;

    @Override
    public void initialise() {
        tradingScreen = (TradingScreen) nuiManager.createScreen("MetalRenegades:tradingScreen");
    }

    @ReceiveEvent
    public void onToggleInventory(TradeScreenRequestEvent event, EntityRef entity) {
        ResourceUrn activeInteractionScreenUri = InteractionUtil.getActiveInteractionScreenUri(entity);
        if (activeInteractionScreenUri != null) {
            InteractionUtil.cancelInteractionAsClient(entity);
        }

        nuiManager.toggleScreen("MetalRenegades:tradingScreen");
    }

    @ReceiveEvent
    public void onTradingScreenAction(TradeScreenRequestEvent event, EntityRef citizen) {
        List<MarketItem> citizenItems = new ArrayList<>();
        for (int i = 0; i < inventoryManager.getNumSlots(citizen); i++) {
            EntityRef entity = inventoryManager.getItemInSlot(citizen, i);
            if (entity.getParentPrefab() != null) {
                MarketItem item = MarketItemBuilder.get(entity.getParentPrefab().getName(), 1);
                citizenItems.add(item);
            }
        }

        List<MarketItem> playerItems = new ArrayList<>();
        EntityRef player = localPlayer.getCharacterEntity();
        for (int i = 0; i < inventoryManager.getNumSlots(player); i++) {
            EntityRef entity = inventoryManager.getItemInSlot(player, i);
            if (entity.getParentPrefab() != null) {
                MarketItem item = MarketItemBuilder.get(entity.getParentPrefab().getName(), 1);
                playerItems.add(item);
            }
        }

        tradingScreen.setCitizenItems(citizenItems);
        tradingScreen.setPlayerItems(playerItems);
    }

    public boolean trade(MarketItem player, MarketItem citizen) {
        return true;
    }

    public boolean isAcceptable(MarketItem player, MarketItem citizen) {
        return true;
    }
}
