// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.metalrenegades.economy.systems;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.gestalt.assets.management.AssetManager;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.module.inventory.systems.InventoryManager;
import org.terasology.engine.logic.inventory.ItemComponent;
import org.terasology.engine.logic.inventory.events.GiveItemEvent;
import org.terasology.engine.registry.In;
import org.terasology.engine.world.block.entity.BlockCommands;
import org.terasology.engine.world.block.items.BlockItemComponent;
import org.terasology.math.TeraMath;
import org.terasology.metalrenegades.economy.events.TradeRequest;
import org.terasology.metalrenegades.economy.events.TradeResponse;
import org.terasology.metalrenegades.economy.ui.MarketItem;

import java.util.Random;
import java.util.Set;

/**
 * Manages requests and responses for player-citizen trades.
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class TradingSystem extends BaseComponentSystem {

    /**
     * Maximum percentage difference between two values for them to be considered about equal
     */
    private static final int MARGIN_PERCENTAGE = 20;

    /**
     * Probability that a trade will be accepted, provided the costs are about equal
     */
    private static final int PROBABILITY = 80;

    @In
    private InventoryManager inventoryManager;

    @In
    private BlockCommands blockCommands;

    @In
    private AssetManager assetManager;

    @In
    private EntityManager entityManager;

    private Logger logger = LoggerFactory.getLogger(TradingSystem.class);

    /**
     * Start the trading process for the specified items
     */
    @ReceiveEvent
    public void onTradeRequest(TradeRequest request, EntityRef character) {

        if (!isAcceptable(request.pItem, request.cItem)) {
            character.send(new TradeResponse(false, "I can't accept that offer!"));
            return;
        }

        if (!isTradeAccepted()) {
            character.send(new TradeResponse(false, "I'm not in the mood to trade."));
            return;
        }

        try {
            // remove item from citizen's inventory
            remove(request.cItem, request.target);

            // add item to player's inventory
            add(request.cItem, character);

            // remove item from player's inventory
            remove(request.pItem, character);

            // add item to citizen's inventory
            add(request.pItem, request.target);
        } catch (Exception e) {
            logger.error("Trade failed. Exception: {}", e.getMessage());
            character.send(new TradeResponse(false, "Trade failed"));
            return;
        }

        character.send(new TradeResponse(true, "I have accepted your offer."));
    }

    /**
     * Remove an item from the specified entity's inventory
     *
     * @param item: MarketItem to be removed
     * @param entity: Entity to be removed from
     */
    private void remove(MarketItem item, EntityRef entity) throws Exception {
        EntityRef itemEntity = EntityRef.NULL;
        for (int i = 0; i < inventoryManager.getNumSlots(entity); i++) {
            EntityRef current = inventoryManager.getItemInSlot(entity, i);

            if (EntityRef.NULL.equals(current)) {
                continue;
            }

            if (item.name.equalsIgnoreCase(current.getParentPrefab().getName())) {
                itemEntity = current;
            } else if (current.hasComponent(BlockItemComponent.class)) {
                if (current.getComponent(BlockItemComponent.class).blockFamily.getURI().toString().equalsIgnoreCase(item.name)) {
                    itemEntity = current;
                }
            }
        }

        if (itemEntity.equals(EntityRef.NULL)) {
            String error = "Could not remove block " + item.name + " from inventory " + entity;
            throw new Exception(error);
        }

        inventoryManager.removeItem(entity, EntityRef.NULL, itemEntity, true, 1);
    }

    /**
     * Add an item to the specified entity's inventory
     *
     * @param item: MarketItem to be added
     * @param entity: Entity to be added to
     * @throws Exception if addition of block to inventory fails
     */
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

    /**
     * Determines if two costs are about equal, depending on MARGIN_PERCENTAGE
     *
     * @param pCost: Integer cost of the player's item
     * @param cCost: Integer cost of the citizen's item
     * @return boolean indicating if the two costs are about equal
     */
    private boolean isAboutEqual(int pCost, int cCost) {
        int delta = TeraMath.fastAbs(pCost - cCost);
        return ((float) (delta / cCost) * 100) < MARGIN_PERCENTAGE;
    }

    /**
     * Calculates if the trade is acceptable to the citizen based only on market costs.
     *
     * @param pItem: MarketItem for the player's item
     * @param cItem: MarketItem for the citizen's item
     * @return boolean indicating if the trade is acceptable or not
     */
    public boolean isAcceptable(MarketItem pItem, MarketItem cItem) {
        return isAboutEqual(pItem.cost, cItem.cost);
    }

    /**
     * Calculates if an acceptable trade is performed based on chance.
     *
     * @return boolean indicating if the trade is accepted.
     */
    public boolean isTradeAccepted() {
        Random rnd = new Random();
        return rnd.nextInt(100) < PROBABILITY;
    }
}
