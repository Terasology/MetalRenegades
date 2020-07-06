// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.metalrenegades.interaction.component;

import org.terasology.entitySystem.Component;

/**
 * Indicates a water well entity, that the player can drink from.
 */
public class WellSourceComponent implements Component {

    /**
     * The number of water refills remaining in this well. Replenishes after a certain period of time.
     */
    public int refillsLeft;

    /**
     * The maximum number of refills that this well can have.
     */
    public int capacity;

    /**
     * Attempts to use one refill from this well.
     *
     * @return True if this is successful, false if this well is empty.
     */
    public boolean useRefill() {
        if (refillsLeft <= 0) {
            return false;
        }
        refillsLeft--;
        return true;
    }

    /**
     * Attempts to add one refill to this well.
     *
     * @return True if this is successful, false if this well is already full.
     */
    public boolean addRefill() {
        if (refillsLeft >= capacity) {
            return false;
        }
        refillsLeft++;
        return true;
    }

}
