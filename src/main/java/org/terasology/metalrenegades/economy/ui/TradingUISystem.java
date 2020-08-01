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

import org.terasology.assets.ResourceUrn;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.characters.interactions.InteractionUtil;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.inventory.ItemComponent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.metalrenegades.economy.events.TradeResponse;
import org.terasology.metalrenegades.economy.events.TradeScreenRequestEvent;
import org.terasology.registry.In;
import org.terasology.registry.Share;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.world.block.items.BlockItemComponent;

import java.util.ArrayList;
import java.util.List;

/**
 * System which handles the data presented to the TradingScreen
 */
@Share(TradingUISystem.class)
@RegisterSystem(RegisterMode.CLIENT)
public class TradingUISystem extends BaseComponentSystem {

    @In
    private NUIManager nuiManager;

    @In
    private LocalPlayer localPlayer;

    @In
    private MarketItemRegistry marketItemRegistry;

    @In
    private InventoryManager inventoryManager;

    /**
     * Citizen entity that the player is trading with
     */
    private EntityRef targetCitizen = EntityRef.NULL;

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
        targetCitizen = citizen;
        refreshLists();
    }

    @ReceiveEvent
    public void onTradeResponse(TradeResponse response, EntityRef entity) {
        if (!entity.equals(localPlayer.getCharacterEntity())) { // checks if event is intended for a different character; occurs when client is hosting.
            return;
        }

        tradingScreen.setMessage(response.message);
        refreshLists();
    }

    /**
     * Calls appropriate functions to update player and citizen's inventories in the UI
     */
    public void refreshLists() {
        refreshCitizenList();
        refreshPlayerList();
    }

    /**
     * Update the content in the citizen's inventory UIList
     */
    private void refreshCitizenList() {
        if (targetCitizen == EntityRef.NULL) {
            return;
        }

        List<MarketItem> items = new ArrayList<>();
        for (int i = 0; i < inventoryManager.getNumSlots(targetCitizen); i++) {
            EntityRef entity = inventoryManager.getItemInSlot(targetCitizen, i);

            if (entity.getParentPrefab() != null) {
                MarketItem item;
                int quantity = inventoryManager.getStackSize(entity);

                if (entity.hasComponent(BlockItemComponent.class)) {
                    String itemName = entity.getComponent(BlockItemComponent.class).blockFamily.getURI().toString();
                    item = marketItemRegistry.get(itemName, quantity);
                } else {
                    item = marketItemRegistry.get(entity.getParentPrefab().getName(), quantity);
                }

                items.add(item);
            }
        }

        tradingScreen.setCitizenItems(items);
    }

    /**
     * Update the content in the player's inventory UIList
     */
    private void refreshPlayerList() {
        List<MarketItem> items = new ArrayList<>();
        EntityRef player = localPlayer.getCharacterEntity();
        for (int i = 0; i < inventoryManager.getNumSlots(player); i++) {
            EntityRef entity = inventoryManager.getItemInSlot(player, i);

            if (entity.getParentPrefab() != null) {
                MarketItem item;

                if (entity.hasComponent(BlockItemComponent.class)) {
                    String itemName = entity.getComponent(BlockItemComponent.class).blockFamily.getURI().toString();
                    item = marketItemRegistry.get(itemName, 1);
                } else {
                    item = marketItemRegistry.get(entity.getParentPrefab().getName(), 1);
                }

                items.add(item);
            }
        }

        tradingScreen.setPlayerItems(items);
    }

    public EntityRef getTargetCitizen() {
        return targetCitizen;
    }
}
