// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.metalrenegades.economy.component;

import org.terasology.gestalt.entitysystem.component.Component;

import java.util.List;

/**
 * Attached to a prefab which lists information about items that can be bought/sold in the market. Items are defined
 * as an array of four strings in the form [itemURI, displayName, description, cost].
 */
public class MarketPriceConfigurationComponent implements Component<MarketPriceConfigurationComponent> {

    /**
     * A list of all item definitions in this component.
     */
    public List<List<String>> items;

}
