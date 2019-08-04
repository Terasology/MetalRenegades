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
import org.terasology.assets.management.AssetManager;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.characters.interactions.InteractionUtil;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.inventory.ItemComponent;
import org.terasology.logic.inventory.events.GiveItemEvent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.math.TeraMath;
import org.terasology.metalrenegades.economy.events.TradeScreenRequestEvent;
import org.terasology.registry.In;
import org.terasology.registry.Share;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.world.block.entity.BlockCommands;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

@Share(TradingUISystem.class)
@RegisterSystem(RegisterMode.CLIENT)
public class TradingUISystem extends BaseComponentSystem {

    @In
    private NUIManager nuiManager;

    @In
    private InventoryManager inventoryManager;

    @In
    private LocalPlayer localPlayer;

    @In
    private AssetManager assetManager;

    @In
    private EntityManager entityManager;

    @In
    private BlockCommands blockCommands;

    private final int PROBABILITY = 50;
    private final int MARGIN_PERCENTAGE = 20;

    private TradingScreen tradingScreen;
    private EntityRef targetCitizen = EntityRef.NULL;

    private Logger logger = LoggerFactory.getLogger(TradingUISystem.class);

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

    public boolean trade(MarketItem pItem, MarketItem cItem) {
        if (targetCitizen == EntityRef.NULL) {
            return false;
        }

        try {
            // remove item from citizen's inventory
            remove(cItem, targetCitizen);

            // add item to player's inventory
            add(cItem, localPlayer.getCharacterEntity());

            // remove item from player's inventory
            remove(pItem, localPlayer.getCharacterEntity());

            // add item to citizen's inventory
            add(pItem, targetCitizen);
        } catch (Exception e) {
            logger.error("Trade failed. Exception: {}", e.getMessage());
            return false;
        }

        return true;
    }

    public boolean isAcceptable(MarketItem pItem, MarketItem cItem) {
        Random rnd = new Random();
        return isAboutEqual(pItem.cost, cItem.cost) && (rnd.nextInt(100) < PROBABILITY);
    }

    public void refreshLists() {
        refreshCitizenList();
        refreshPlayerList();
    }

    private boolean isAboutEqual(int pCost, int cCost) {
        int delta = TeraMath.fastAbs(pCost - cCost);
        return ((float)(delta / cCost) * 100) < MARGIN_PERCENTAGE;
    }

    private void refreshCitizenList() {
        if (targetCitizen == EntityRef.NULL) {
            return;
        }

        List<MarketItem> items = new ArrayList<>();
        for (int i = 0; i < inventoryManager.getNumSlots(targetCitizen); i++) {
            EntityRef entity = inventoryManager.getItemInSlot(targetCitizen, i);
            if (entity.getParentPrefab() != null) {
                MarketItem item = MarketItemBuilder.get(entity.getParentPrefab().getName(), 1);
                items.add(item);
            }
        }

        tradingScreen.setCitizenItems(items);
    }

    private void refreshPlayerList() {
        List<MarketItem> items = new ArrayList<>();
        EntityRef player = localPlayer.getCharacterEntity();
        for (int i = 0; i < inventoryManager.getNumSlots(player); i++) {
            EntityRef entity = inventoryManager.getItemInSlot(player, i);
            if (entity.getParentPrefab() != null) {
                MarketItem item = MarketItemBuilder.get(entity.getParentPrefab().getName(), 1);
                items.add(item);
            }
        }

        tradingScreen.setPlayerItems(items);
    }

    private void remove(MarketItem item, EntityRef entity) {
        EntityRef itemEntity = EntityRef.NULL;
        for (int i = 0; i < inventoryManager.getNumSlots(entity); i++) {
            EntityRef current = inventoryManager.getItemInSlot(entity, i);
            if (current != EntityRef.NULL
                    && item.name.equalsIgnoreCase(current.getParentPrefab().getName())) {
                itemEntity = current;
                break;
            }
        }
        inventoryManager.removeItem(entity, EntityRef.NULL, itemEntity, true, 1);
    }

    private void add(MarketItem item, EntityRef entity) throws Exception {
        Set<ResourceUrn> matches = assetManager.resolve(item.name, Prefab.class);

        if (matches.size() == 1) {
            Prefab prefab = assetManager.getAsset(matches.iterator().next(), Prefab.class).orElse(null);
            if (prefab != null && prefab.getComponent(ItemComponent.class) != null) {
                EntityRef itemEntity = entityManager.create(prefab);
                if (itemEntity != EntityRef.NULL) {
                    itemEntity.send(new GiveItemEvent(entity));
                    return;
                }
            }
        }

        String message = blockCommands.giveBlock(entity, item.name, 1, null);
        if (message == null) {
            String error = "Could not add block " + item.name + " to inventory " + entity;
            throw new Exception(error);
        }
    }
}
