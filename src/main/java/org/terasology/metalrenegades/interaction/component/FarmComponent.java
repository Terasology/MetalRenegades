// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.metalrenegades.interaction.component;

import org.terasology.gestalt.entitysystem.component.Component;

/**
 * Component attached to farms inside cities that have not yet generated any crops. This component is removed when the
 * farm crops are generated.
 */
public class FarmComponent implements Component<FarmComponent> {

    /**
     * The number of blocks from the centre of the farm that crops can generate on. This number does not include
     * the centre block.
     */
    public int plantableRadius;

    /**
     * The number of world time cycles until the farm should generate crops.
     */
    public int generationCycles = 4;

    /**
     * Upon generation, the chance that any particular square on the farm will have a planted crop.
     */
    public double genChance = 0.2;

    @Override
    public void copyFrom(FarmComponent other) {
        this.plantableRadius = other.plantableRadius;
        this.generationCycles = other.generationCycles;
        this.genChance = other.genChance;
    }
}
