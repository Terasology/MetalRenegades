// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.metalrenegades.interaction.component;

import org.terasology.entitySystem.Component;

/**
 * Component describing the status of particular water cup.
 */
public class WaterCupComponent implements Component {

    /**
     * True if the cup contains water, false otherwise.
     */
    public boolean filled;

}
