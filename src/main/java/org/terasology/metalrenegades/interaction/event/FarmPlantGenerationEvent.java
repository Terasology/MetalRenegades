// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.metalrenegades.interaction.event;

import org.terasology.engine.entitySystem.event.Event;

/**
 * Fired when a city farm has crops generated on it.
 */
public class FarmPlantGenerationEvent implements Event {

    /**
     * The prefab name of the bush/vine that will be generated on the farm.
     */
    public String plantName;

    /**
     * The number of blocks from the centre of the farm that crops will be generated on.
     * This number does not include the centre block.
     */
    public int plantableRadius;

    /**
     * The chance that any particular square on the farm will have a planted crop.
     */
    public double genChance;

    public FarmPlantGenerationEvent(String plant, int radius, double chance) {
        this.plantName = plant;
        this.plantableRadius = radius;
        this.genChance = chance;
    }

}
