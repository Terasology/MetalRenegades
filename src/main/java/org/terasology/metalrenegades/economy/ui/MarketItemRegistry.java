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

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.metalrenegades.economy.component.MarketPriceConfigurationComponent;
import org.terasology.oreGeneration.CustomOreGen;
import org.terasology.oreGeneration.components.OreGenDefinitionComponent;
import org.terasology.registry.CoreRegistry;
import org.terasology.registry.In;
import org.terasology.registry.Share;

import java.util.*;
import java.util.function.Function;

/**
 * Stores information about all market items and prices, and also creates MarketItem objects based on the stored data.
 */
@RegisterSystem(RegisterMode.ALWAYS)
@Share(MarketItemRegistry.class)
public class MarketItemRegistry extends BaseComponentSystem {

    @In
    private PrefabManager prefabManager;

    private final String DEFAULT_ITEM = "default";
    private final String EMPTY = "empty";
    private Map<String, MarketItem> details = new HashMap<>();
    private boolean isInitialised = false;

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

    public MarketItem getDefault() {
        if (!isInitialised) {
            initialiseItems();
        }
        return details.get(DEFAULT_ITEM);
    }

    public MarketItem getEmpty() {
        if (!isInitialised) {
            initialiseItems();
        }
        return details.get(EMPTY);
    }

    public MarketItem get(String name, int quantity) {
        if (!isInitialised) {
            initialiseItems();
        }

        MarketItem item = details.get(name);
        if (item == null) {
            item = tryCreate(name);
        }

        if  (item == null) {
            item = details.get(DEFAULT_ITEM);
        }

        item.quantity = quantity;
        item.buyable = true;
        item.sellable = false;
        return item;
    }

    private MarketItem tryCreate(String name) {
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
