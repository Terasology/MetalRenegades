// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.metalrenegades.combat.component;

import org.terasology.entitySystem.Component;

public class EnemyGracePeriodComponent implements Component {

    public int cyclesLeft;

    public EnemyGracePeriodComponent(int cycles) {
        this.cyclesLeft = cycles;
    }

    public EnemyGracePeriodComponent() {
        this.cyclesLeft = 5;
    }

}
