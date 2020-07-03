// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.metalrenegades.population.component;

import org.terasology.entitySystem.Component;

public class PopulationComponent implements Component {
    public int goodCitizens;
    public int badCitizens;
    public int neutralCitizens;

    public PopulationComponent(){
        this.badCitizens=0;
        this.goodCitizens=0;
        this.neutralCitizens = 0;
    }
}
