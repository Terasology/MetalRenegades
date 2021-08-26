// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.metalrenegades.interaction.component;

import org.terasology.gestalt.entitysystem.component.Component;

/**
 * Component describing the status of particular water cup.
 */
public class WaterCupComponent implements Component<WaterCupComponent> {

    /**
     * True if the cup contains water, false otherwise.
     */
    public boolean filled;

    @Override
    public void copyFrom(WaterCupComponent other) {
        this.filled = other.filled;
    }
}
