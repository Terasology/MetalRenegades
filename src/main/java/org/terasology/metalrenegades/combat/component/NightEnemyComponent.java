// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.metalrenegades.combat.component;

import org.terasology.gestalt.entitysystem.component.Component;

/**
 * Marks a nighttime enemy character, that will be removed when the time cycle reaches daytime.
 */
public class NightEnemyComponent implements Component<NightEnemyComponent> {

    @Override
    public void copyFrom(NightEnemyComponent other) {

    }
}
