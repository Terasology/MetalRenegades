// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.metalrenegades.population.system;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.lifecycleEvents.OnActivatedComponent;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.metalrenegades.ai.component.FactionAlignmentComponent;
import org.terasology.metalrenegades.ai.event.CitizenSpawnedEvent;
import org.terasology.metalrenegades.ai.system.FactionAlignmentSystem;
import org.terasology.metalrenegades.population.component.PopulationComponent;
import org.terasology.network.ClientComponent;
import org.terasology.registry.In;

@RegisterSystem(value = RegisterMode.AUTHORITY)
public class PopulationSystem extends BaseComponentSystem {

    @In
    LocalPlayer player;

    Logger logger = LoggerFactory.getLogger(PopulationSystem.class);

    @ReceiveEvent
    public void onPlayerSpawn(OnActivatedComponent event, EntityRef entity, ClientComponent component) {

        entity.addComponent(new PopulationComponent());
        logger.error("Component PopulationComponent added to player");


    }

    @ReceiveEvent
    public void citizenSpawned(CitizenSpawnedEvent event, EntityRef citizen, FactionAlignmentComponent factionAlignmentComponent) {
        PopulationComponent populationComponent = player.getClientEntity().getComponent(PopulationComponent.class);
        if (factionAlignmentComponent.alignment == FactionAlignmentSystem.Alignment.NEUTRAL) {
            populationComponent.neutralCitizens++;

        } else if (factionAlignmentComponent.alignment == FactionAlignmentSystem.Alignment.GOOD) {
            populationComponent.goodCitizens++;

        } else if (factionAlignmentComponent.alignment == FactionAlignmentSystem.Alignment.BAD) {
            populationComponent.badCitizens++;
        } else {
            logger.error("Invalid Faction Alignment");
        }

        player.getClientEntity().saveComponent(populationComponent);
        logger.error("Population is {} {} {] ",populationComponent.badCitizens, populationComponent.goodCitizens, populationComponent.neutralCitizens);
    }


}
