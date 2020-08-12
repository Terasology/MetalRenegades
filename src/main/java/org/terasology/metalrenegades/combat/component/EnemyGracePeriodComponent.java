// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.metalrenegades.combat.component;

import org.terasology.entitySystem.Component;

/**
 * Attaches to a player who just respawned, preventing enemies from spawning around them even if they are outside
 * a city at nighttime, for a limited time.
 */
public class EnemyGracePeriodComponent implements Component {

    public int cyclesLeft;

    public EnemyGracePeriodComponent(int cycles) {
        this.cyclesLeft = cycles;
    }

    public EnemyGracePeriodComponent() {
        this.cyclesLeft = 5;
    }

}
