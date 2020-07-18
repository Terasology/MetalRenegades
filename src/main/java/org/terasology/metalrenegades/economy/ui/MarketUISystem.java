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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.ResourceUrn;
import org.terasology.economy.events.MarketInfoClientRequestEvent;
import org.terasology.economy.events.MarketInfoClientResponseEvent;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.characters.interactions.InteractionUtil;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.metalrenegades.economy.events.MarketScreenRequestEvent;
import org.terasology.metalrenegades.economy.events.TransactionType;
import org.terasology.metalrenegades.economy.events.UpdateMarketScreenEvent;
import org.terasology.registry.In;
import org.terasology.registry.Share;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.world.block.items.BlockItemComponent;

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
            MarketItem item = MarketItemBuilder.get(entry.getKey(), entry.getValue());
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
        } else if (type == TransactionType.SELLING){
            EntityRef player = localPlayer.getCharacterEntity();
            int slots = inventoryManager.getNumSlots(player);

            for (int i = 0; i < slots; i++) {
                EntityRef entity = inventoryManager.getItemInSlot(player, i);
                if (entity.getParentPrefab() != null) {
                    MarketItem item;
                    logger.info(entity.getParentPrefab().getName() + " == " + "blockItemBase");
                    if (entity.getParentPrefab().getName().equalsIgnoreCase("engine:blockItemBase")) {
                        item = MarketItemBuilder.get(entity.getComponent(BlockItemComponent.class).blockFamily.getURI().toString(), inventoryManager.getStackSize(entity));
                    } else {
                        item = MarketItemBuilder.get(entity.getParentPrefab().getName(), inventoryManager.getStackSize(entity));
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
