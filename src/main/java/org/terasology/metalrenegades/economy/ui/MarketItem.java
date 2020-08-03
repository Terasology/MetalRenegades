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
