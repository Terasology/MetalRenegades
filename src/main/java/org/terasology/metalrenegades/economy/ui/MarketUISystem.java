// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.metalrenegades.economy.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.economy.events.MarketInfoClientRequestEvent;
import org.terasology.economy.events.MarketInfoClientResponseEvent;
import org.terasology.engine.entitySystem.entity.EntityManager;
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
import org.terasology.engine.world.time.WorldTimeEvent;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;
import org.terasology.metalrenegades.economy.events.MarketScreenRequestEvent;
import org.terasology.metalrenegades.economy.events.TransactionType;
import org.terasology.metalrenegades.economy.events.UpdateMarketScreenEvent;
import org.terasology.module.inventory.systems.InventoryManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Populates the market screen
 */
@Share(MarketUISystem.class)
@RegisterSystem(RegisterMode.CLIENT)
public class MarketUISystem extends BaseComponentSystem {

    @In
    private NUIManager nuiManager;

    @In
    private EntityManager entityManager;

    @In
    private InventoryManager inventoryManager;

    @In
    private MarketItemRegistry marketItemRegistry;

    @In
    private LocalPlayer localPlayer;

    private Logger logger = LoggerFactory.getLogger(MarketUISystem.class);

    private MarketScreen marketScreen;

    private long marketID;

    private TransactionType type;

    @Override
    public void initialise() {
        marketScreen = (MarketScreen) nuiManager.createScreen("MetalRenegades:marketScreen");
    }

    @ReceiveEvent
    public void onWorldTimeCycle(WorldTimeEvent worldTimeEvent, EntityRef entity) {
        if (marketID != 0 && type != null) {
            updateScreenInformation();
        }
    }

    @ReceiveEvent
    public void onToggleInventory(MarketScreenRequestEvent event, EntityRef entity) {
        ResourceUrn activeInteractionScreenUri = InteractionUtil.getActiveInteractionScreenUri(entity);
        if (activeInteractionScreenUri != null) {
            InteractionUtil.cancelInteractionAsClient(entity);
        }

        nuiManager.toggleScreen("MetalRenegades:marketScreen");
    }

    @ReceiveEvent
    public void onMarketScreenAction(MarketScreenRequestEvent event, EntityRef entityRef) {
        List<MarketItem> marketItemList = new ArrayList<>();
        marketScreen.setItemList(marketItemList); // clear out any old item listings from the UI.

        this.marketID = event.market;
        this.type = event.type;

        updateScreenInformation();

        marketScreen.setType(event.type);
    }

    @ReceiveEvent
    public void onResourceInfoResponse(MarketInfoClientResponseEvent marketInfoResponseEvent, EntityRef character) {
        List<MarketItem> marketItemList = new ArrayList<>();
        Map<String, Integer> resources;

        resources = marketInfoResponseEvent.resources;

        for (Map.Entry<String, Integer> entry : resources.entrySet()) {
            MarketItem item = marketItemRegistry.get(entry.getKey(), entry.getValue());
            marketItemList.add(item);
        }

        marketScreen.setItemList(marketItemList);
    }

    @ReceiveEvent
    public void onUpdateScreenEvent(UpdateMarketScreenEvent event, EntityRef entity) {
        updateScreenInformation();
    }

    private void updateScreenInformation() {
        List<MarketItem> marketItemList = new ArrayList<>();

        if (type == TransactionType.BUYING) {
            localPlayer.getCharacterEntity().send(new MarketInfoClientRequestEvent(marketID));
        } else if (type == TransactionType.SELLING) {
            EntityRef player = localPlayer.getCharacterEntity();
            int slots = inventoryManager.getNumSlots(player);

            for (int i = 0; i < slots; i++) {
                EntityRef entity = inventoryManager.getItemInSlot(player, i);
                if (entity.getParentPrefab() != null) {
                    MarketItem item;
                    logger.info(entity.getParentPrefab().getName() + " == " + "blockItemBase");
                    if (entity.getParentPrefab().getName().equalsIgnoreCase("engine:blockItemBase")) {
                        item = marketItemRegistry.get(entity.getComponent(BlockItemComponent.class).blockFamily.getURI().toString(),
                                inventoryManager.getStackSize(entity));
                    } else {
                        item = marketItemRegistry.get(entity.getParentPrefab().getName(), inventoryManager.getStackSize(entity));
                    }
                    marketItemList.add(item);
                }
            }
            marketScreen.setItemList(marketItemList);
        } else {
            logger.warn("TransactionType not recognised.");
        }
    }

}
