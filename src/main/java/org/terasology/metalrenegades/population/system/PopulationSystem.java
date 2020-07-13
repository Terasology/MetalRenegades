// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.metalrenegades.population.system;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.dynamicCities.buildings.components.SettlementRefComponent;
import org.terasology.dynamicCities.settlements.events.SettlementRegisterEvent;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.metalrenegades.ai.component.FactionAlignmentComponent;
import org.terasology.metalrenegades.ai.component.HomeComponent;
import org.terasology.metalrenegades.ai.event.CitizenSpawnedEvent;
import org.terasology.metalrenegades.population.component.FactionDistributionComponent;

@RegisterSystem(value = RegisterMode.AUTHORITY)
public class PopulationSystem extends BaseComponentSystem {


    Logger logger = LoggerFactory.getLogger(PopulationSystem.class);

    @ReceiveEvent
    public void onSettlementRegisterEvent(SettlementRegisterEvent buildingEntitySpawnedEvent, EntityRef entityRef) {
        entityRef.addComponent(new FactionDistributionComponent());
    }

    @ReceiveEvent
    public void citizenSpawned(CitizenSpawnedEvent event, EntityRef citizen,
                               FactionAlignmentComponent factionAlignmentComponent, HomeComponent homeComponent) {

        EntityRef homeEntity = homeComponent.building;
        SettlementRefComponent settlementRefComponent = homeEntity.getComponent(SettlementRefComponent.class);
        EntityRef settlementEntity = settlementRefComponent.settlement;
        FactionDistributionComponent populationComponent =
                settlementEntity.getComponent(FactionDistributionComponent.class);


        switch (factionAlignmentComponent.alignment) {
            case NEUTRAL:
                populationComponent.neutralCitizens++;
                break;
            case GOOD:
                populationComponent.goodCitizens++;
                break;
            case BAD:
                populationComponent.badCitizens++;
                break;
            default:
                logger.error("Invalid Faction Alignment");
        }

        settlementEntity.saveComponent(populationComponent);
    }


}
