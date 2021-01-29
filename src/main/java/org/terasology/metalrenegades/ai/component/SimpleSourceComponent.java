// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.metalrenegades.ai.component;

import org.terasology.entitySystem.Component;
import org.terasology.metalrenegades.ai.CitizenNeed;

/**
 * Inidicates an entity that can act as a simple need source.
 */
public class SimpleSourceComponent implements Component {

    public CitizenNeed.Type needType;

    public SimpleSourceComponent(CitizenNeed.Type needType) {
        this.needType = needType;
    }

    public SimpleSourceComponent() {
        this.needType = null;
    }

}
