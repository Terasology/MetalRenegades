// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.metalrenegades.population.component;

import org.terasology.entitySystem.Component;

/**
 * Track total population information of good, bad, and neutral citizens.
 *
 * The entity this component belongs to defines the scope of the tracked population. 
 * For instance, it may be attached to a world entity for the overall population or a settlement entity for the population of that settlement.
 *
 * The population is usually tracked by the {@link PopulationSystem}.
 */
public class FactionDistributionComponent implements Component {
    public int goodCitizens;
    public int badCitizens;
    public int neutralCitizens;

    public FactionDistributionComponent(){
        this.badCitizens = 0;
        this.goodCitizens = 0;
        this.neutralCitizens = 0;
    }
}
