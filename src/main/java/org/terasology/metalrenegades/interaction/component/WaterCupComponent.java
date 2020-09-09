// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.metalrenegades.interaction.component;

import org.terasology.engine.entitySystem.Component;

/**
 * Component describing the status of particular water cup.
 */
public class WaterCupComponent implements Component {

    /**
     * True if the cup contains water, false otherwise.
     */
    public boolean filled;

}
