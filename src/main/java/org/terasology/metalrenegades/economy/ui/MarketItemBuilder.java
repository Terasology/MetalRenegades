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

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Creates a MarketItem object based on the provided data
 */
public final class MarketItemBuilder {

    private static final String DEFAULT_ITEM = "default";
    private static final String EMPTY = "empty";
    private static Map<String, MarketItem> details = new HashMap<>();
    private static boolean isInitialised = false;

    private static void initialise() {
        details.put("waffles", new MarketItem(
                "waffles",
                "Hot and fresh waffles.",
                50
        ));

        details.put(DEFAULT_ITEM, new MarketItem(
                "default_item",
                "The item you requested doesn't have any info",
                0
        ));

        details.put(EMPTY, new MarketItem(
                "",
                "",
                0
        ));

        isInitialised = true;
    }

    public static MarketItem getDefault() {
        if (!isInitialised) {
            initialise();
        }
        return details.get(DEFAULT_ITEM);
    }

    public static MarketItem getEmpty() {
        if (!isInitialised) {
            initialise();
        }
        return details.get(EMPTY);
    }

    public static MarketItem get(String name, int quantity) {
        if (!isInitialised) {
            initialise();
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

    private static MarketItem tryCreate(String name) {
        Random random = new Random();
        MarketItem item = new MarketItem(
                name,
                "A mystery item. No description is available",
                random.nextInt(50)
        );
        details.put(name, item);
        return item;
    }
}
