// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.metalrenegades.ai.component;

import org.terasology.gestalt.entitysystem.component.Component;
import org.terasology.metalrenegades.ai.CitizenNeed;

/**
 * Inidicates an entity that can act as a simple need source.
 */
public class SimpleSourceComponent implements Component<SimpleSourceComponent> {
    public CitizenNeed.Type needType;

    @Override
    public void copyFrom(SimpleSourceComponent other) {
        this.needType = other.needType;
    }
}
