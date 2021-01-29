// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.metalrenegades.ai.component;

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;

import java.util.List;

/**
 * Stores the faction enemies in range of this citizen.
 */
public class NearbyCitizenEnemiesComponent implements Component {

    public float searchRadius = 20f;

    public List<EntityRef> enemiesWithinRange;

    public EntityRef closestEnemy;

}
