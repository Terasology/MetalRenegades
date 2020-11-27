// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.metalrenegades.economy.ui;

/**
 * Data class holding all information about items bought or sold in the market
 */
public class MarketItem {
    public String name;
    public String displayName;
    public String description;
    public int cost;
    public int quantity;
    public boolean buyable;
    public boolean sellable;

    public MarketItem(String name, String displayName, String description, int cost) {
        this.name = name;
        this.displayName = displayName;
        this.description = description;
        this.cost = cost;
    }

    public MarketItem() {
    }

}
