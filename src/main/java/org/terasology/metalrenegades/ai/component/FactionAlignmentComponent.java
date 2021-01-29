// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.metalrenegades.ai.component;

import org.terasology.entitySystem.Component;
import org.terasology.metalrenegades.ai.system.FactionAlignmentSystem.Alignment;

/**
 * Defines the faction alignment of a particular character, building, or settlement.
 */
public class FactionAlignmentComponent implements Component {

    public Alignment alignment;

    public FactionAlignmentComponent() {
        this.alignment = Alignment.NEUTRAL;
    }

    public FactionAlignmentComponent(Alignment alignment) {
        this.alignment = alignment;
    }

}
