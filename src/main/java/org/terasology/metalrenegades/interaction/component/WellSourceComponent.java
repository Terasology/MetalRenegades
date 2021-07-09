// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.metalrenegades.interaction.component;

import org.terasology.gestalt.entitysystem.component.Component;

/**
 * Indicates a water well entity, that the player can drink from.
 */
public class WellSourceComponent implements Component<WellSourceComponent> {

    /**
     * The number of water refills remaining in this well. Replenishes after a certain period of time.
     */
    public int refillsLeft;

    /**
     * The maximum number of refills that this well can have.
     */
    public int capacity;

}
