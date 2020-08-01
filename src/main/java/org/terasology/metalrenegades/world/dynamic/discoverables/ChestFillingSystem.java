// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.metalrenegades.world.dynamic.discoverables;

import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.inventory.InventoryComponent;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.inventory.ItemComponent;
import org.terasology.registry.In;
import org.terasology.utilities.random.FastRandom;
import org.terasology.utilities.random.Random;

/**
 * Checks for any chests that have a {@link DiscoverableChestComponent} attached and fills it with items.
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class ChestFillingSystem extends BaseComponentSystem implements UpdateSubscriberSystem {

    /**
     * Items that generate in chests as stacks of five.
     */
    private final String[] bulkItems = {
            "AdditionalFruits:Blueberry",
            "AdditionalFruits:Raspberry",
            "AdditionalFruits:Strawberry",
            "AdditionalVegetables:Potato",
            "MetalRenegades:bulletItem"
    };

    /**
     * Items that generate in chests as stacks of one.
     */
    private final String[] singleItems = {
            "CoreAssets:pickaxe",
            "CoreAssets:axe",
            "CoreAssets:shovel",
            "MetalRenegades:emptyCup",
            "MetalRenegades:pistol"
    };

    /**
     * The chance out of one that any particular chest slot will contain an item.
     */
    private static final float ITEM_CHANCE = 0.08f;

    /**
     * The chance out of one that a chest slot with an item in it will be a bulk item vs. a single item.
     */
    private static final float BULK_SINGLE_RATIO = 0.75f;

    /**
     * The random number provider that determines the items generated inside a discoverable chest.
     */
    private Random random;

    @In
    private EntityManager entityManager;

    @In
    private InventoryManager inventoryManager;

    @Override
    public void initialise() {
        random = new FastRandom();
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
        EntityRef item;
        if (random.nextFloat() < BULK_SINGLE_RATIO) {
            item = entityManager.create(bulkItems[random.nextInt(bulkItems.length)]);
            ItemComponent itemComponent = item.getComponent(ItemComponent.class);
            itemComponent.stackCount = 5;
            item.saveComponent(itemComponent);
        } else {
            item = entityManager.create(singleItems[random.nextInt(singleItems.length)]);
        }
        return item;
    }
}
