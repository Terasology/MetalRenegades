// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.metalrenegades.ai.component;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.entitySystem.entity.EntityRef;

/**
 * A component attached to citizens to provide information about their current shelter.
 */
public class HomeComponent implements Component {

    /**
     * The building entity of this citizen's home.
     */
    public EntityRef building;

}
