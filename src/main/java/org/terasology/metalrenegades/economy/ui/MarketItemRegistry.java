// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.metalrenegades.economy.ui;

import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.metalrenegades.economy.component.MarketPriceConfigurationComponent;
import org.terasology.registry.CoreRegistry;
import org.terasology.registry.In;
import org.terasology.registry.Share;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Stores information about all market items and prices, and also creates MarketItem objects based on the stored data.
 */
@RegisterSystem(RegisterMode.ALWAYS)
@Share(MarketItemRegistry.class)
public class MarketItemRegistry extends BaseComponentSystem {

    @In
    private PrefabManager prefabManager;

    private final String EMPTY = "empty";
    private Map<String, MarketItem> details = new HashMap<>();
    private boolean isInitialised = false;

    /**
     * Initializes all items present inside prefabs with {@link MarketPriceConfigurationComponent}. An item is initialized
     * using an array of four strings in the form [itemURI, displayName, description, cost].
     */
    public void initialiseItems() {
        details.put(EMPTY, new MarketItem(
                "",
                "",
                "",
                0
        ));

        if (prefabManager == null) {
            prefabManager = CoreRegistry.get(PrefabManager.class);
        }

        for (Prefab prefab : prefabManager.listPrefabs(MarketPriceConfigurationComponent.class)) {
            MarketPriceConfigurationComponent priceConfig = prefab.getComponent(MarketPriceConfigurationComponent.class);
            List<List<String>> configs = priceConfig.items;

            configs.forEach(config -> {
                Iterator<String> it = config.iterator();

                String properName = it.next();
                details.put(properName, new MarketItem(properName, it.next(), it.next(), Integer.parseInt(it.next())));
            });
        }

        isInitialised = true;
    }

    /**
     * Returns an empty item, used in the UI system to represent no object selected.
     *
     * @return An empty market item.
     */
    public MarketItem getEmpty() {
        if (!isInitialised) {
            initialiseItems();
        }
        return details.get(EMPTY);
    }

    /**
     * Returns a market item with details from the registry.
     *
     * @param name The URI of this item.
     * @param quantity The quantity of this item.
     * @return A market item object with provided and registry values.
     */
    public MarketItem get(String name, int quantity) {
        if (!isInitialised) {
            initialiseItems();
        }

        MarketItem item = details.get(name);
        if (item == null) {
            item = createGeneric(name);
        }

        item.quantity = quantity;
        item.buyable = true;
        item.sellable = false;
        return item;
    }

    /**
     * Creates a generic market item with the provided name and quantity, for when the registry has no information
     * about this item URI.
     *
     * @param name The URI of this item.
     * @return A market item object with generic values for this URI.
     */
    private MarketItem createGeneric(String name) {
        Random random = new Random();
        MarketItem item = new MarketItem(
                name,
                name,
                "A mystery item. No description is available",
                random.nextInt(50)
        );
        details.put(name, item);
        return item;
    }
}
