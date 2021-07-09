// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.metalrenegades.ai.component;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.gestalt.entitysystem.component.Component;

import java.util.List;

/**
 * Stores the faction enemies in range of this citizen.
 */
public class NearbyCitizenEnemiesComponent implements Component<NearbyCitizenEnemiesComponent> {

    public float searchRadius = 20f;

    public List<EntityRef> enemiesWithinRange;

    public EntityRef closestEnemy;

}
