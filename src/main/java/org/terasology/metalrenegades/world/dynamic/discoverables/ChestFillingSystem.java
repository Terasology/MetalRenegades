// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.metalrenegades.world.dynamic.discoverables;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.inventory.InventoryComponent;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.inventory.ItemComponent;
import org.terasology.metalrenegades.economy.systems.MarketCitizenSpawnSystem;
import org.terasology.registry.In;
import org.terasology.utilities.random.FastRandom;
import org.terasology.utilities.random.Random;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Checks for any chests that have a {@link DiscoverableChestComponent} attached and fills it with items.
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class ChestFillingSystem extends BaseComponentSystem implements UpdateSubscriberSystem {

    private Logger logger = LoggerFactory.getLogger(ChestFillingSystem.class);

    /**
     * The chance out of one that any particular chest slot will contain an item.
     */
    private static final float ITEM_CHANCE = 0.08f;

    private Map<String, Integer> hiddenItemsRegistry = new HashMap<>();

    /**
     * The random number provider that determines the items generated inside a discoverable chest.
     */
    private Random random;

    @In
    private PrefabManager prefabManager;

    @In
    private EntityManager entityManager;

    @In
    private InventoryManager inventoryManager;

    @Override
    public void initialise() {
        random = new FastRandom();

        prefabManager.listPrefabs(DiscoverableItemConfigurationComponent.class).stream().forEach(prefab -> {
            DiscoverableItemConfigurationComponent itemDef =
                    prefab.getComponent(DiscoverableItemConfigurationComponent.class);

            List<List<String>> configs = itemDef.items;
            configs.stream().forEach(config -> {
                Iterator<String> it = config.iterator();
                hiddenItemsRegistry.put(it.next(), Integer.parseInt(it.next()));
            });
        });
    }

    @Override
    public void update(float delta) {
        for (EntityRef chestEntity : entityManager.getEntitiesWith(DiscoverableChestComponent.class)) {
            InventoryComponent inventoryComponent = chestEntity.getComponent(InventoryComponent.class);

            int slots = inventoryManager.getNumSlots(chestEntity);
            boolean containsItem = false;

            for (int i = 0; i < slots; i++) {
                EntityRef slotItem = getSlotItem();
                if (slotContainsItem()) {
                    containsItem = true;
                    inventoryComponent.itemSlots.set(i, getSlotItem());
                }
            }

            if (!containsItem) { // ensures that there is at least one item in each chest
                inventoryComponent.itemSlots.set(0, getSlotItem());
            }

            chestEntity.saveComponent(inventoryComponent);
            chestEntity.removeComponent(DiscoverableChestComponent.class);
        }
    }

    /**
     * Checks if a particular item slot should contain an item.
     *
     * @return True if this slot contains an item, false otherwise.
     */
    private boolean slotContainsItem() {
        return random.nextFloat() < ITEM_CHANCE;
    }

    /**
     * Returns an item entity for a discoverable chest slot.
     *
     * @return The chest slot item.
     */
    private EntityRef getSlotItem() {
        if (hiddenItemsRegistry.isEmpty()) {
            initialise();

            if (hiddenItemsRegistry.isEmpty()) { // still empty after initialization? something must be wrong
                logger.warn("No items in the hidden chest registry!");
            }
        }

        Object[] itemURIs = hiddenItemsRegistry.keySet().toArray();
        String randomURI = (String) itemURIs[random.nextInt(itemURIs.length)];

        EntityRef item = entityManager.create(randomURI);
        ItemComponent itemComponent = item.getComponent(ItemComponent.class);
        itemComponent.stackCount = hiddenItemsRegistry.get(randomURI).byteValue();
        item.saveComponent(itemComponent);

        return item;
    }
}
