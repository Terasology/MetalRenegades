// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.metalrenegades.interaction.component;

import org.terasology.entitySystem.Component;

/**
 * A component attached to settlements that contains the natural crop of that particular settlment.
 */
public class CityCropComponent implements Component {
    public CityCropComponent() {

    }
    public CityCropComponent(String item, String plant) {
        this.itemName = item;
        this.plantName = plant;
    }

    /**
     * The prefab name of the item form of this crop.
     */
    public String itemName;

    /**
     * The prefab name of the block form of this crop.
     */
    public String plantName;

}
