// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.metalrenegades.economy.ui;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.characters.interactions.InteractionUtil;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.registry.In;
import org.terasology.engine.registry.Share;
import org.terasology.engine.rendering.nui.NUIManager;
import org.terasology.engine.world.block.items.BlockItemComponent;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;
import org.terasology.metalrenegades.economy.events.TradeResponse;
import org.terasology.metalrenegades.economy.events.TradeScreenRequestEvent;
import org.terasology.module.inventory.systems.InventoryManager;

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
        // checks if event is intended for a different character; occurs when client is hosting.
        if (!entity.equals(localPlayer.getCharacterEntity())) {
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
