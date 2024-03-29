// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.metalrenegades.interaction.systems;

import org.joml.RoundingMode;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.terasology.dynamicCities.buildings.components.SettlementRefComponent;
import org.terasology.dynamicCities.settlements.events.SettlementRegisterEvent;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.registry.In;
import org.terasology.engine.world.time.WorldTimeEvent;
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;
import org.terasology.metalrenegades.interaction.component.CityCropComponent;
import org.terasology.metalrenegades.interaction.component.FarmComponent;
import org.terasology.metalrenegades.interaction.event.FarmPlantGenerationEvent;
import org.terasology.simpleFarming.events.OnSeedPlanted;

/**
 * This system manages the native crops of settlements, and the generation of crops upon settlement farms.
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class FarmManagementSystem extends BaseComponentSystem {

    /**
     * The number of blocks above the structure template centre that crops should spawn on.
     */
    private static final int PLANT_OFFSET = 2;

    /**
     * The list of all possible native settlement crops. One of these is picked randomly upon settlement generation.
     */
    private static final String[][] CROPS = {
            {"Blueberry", "BlueberryBush"},
            {"Cranberry", "CranberryBush"},
            {"Peach", "PeachBush"},
            {"Raspberry", "RaspberryBush"},
            {"Strawberry", "StrawberryBush"},
            {"Tomato", "TomatoBud"},
            {"Cucumber", "CucumberBud"},
            {"Potato", "PotatoBush"}
    };

    @In
    private EntityManager entityManager;

    @ReceiveEvent
    public void onWorldTimeCycle(WorldTimeEvent worldTimeEvent, EntityRef target) {
        for (EntityRef farm : entityManager.getEntitiesWith(FarmComponent.class)) {
            FarmComponent farmComponent = farm.getComponent(FarmComponent.class);
            farmComponent.generationCycles--;

            if (farmComponent.generationCycles == 0) {
                SettlementRefComponent settlementRefComponent = farm.getComponent(SettlementRefComponent.class);
                CityCropComponent cityCropComponent = settlementRefComponent.settlement.getComponent(CityCropComponent.class);

                farm.send(new FarmPlantGenerationEvent(cityCropComponent.plantName, farmComponent.plantableRadius, farmComponent.genChance));
                farm.removeComponent(FarmComponent.class); // The farm component is deleted upon generation, to avoid re-generation later.
            } else {
                farm.saveComponent(farmComponent);
            }
        }
    }

    @ReceiveEvent
    public void onSettlementRegisterEvent(SettlementRegisterEvent buildingEntitySpawnedEvent, EntityRef entityRef) {
        String[] cityCrop = CROPS[(int) (Math.random() * CROPS.length)];

        CityCropComponent cityCropComponent = new CityCropComponent();
        cityCropComponent.itemName = cityCrop[0];
        cityCropComponent.plantName = cityCrop[1];
        entityRef.addComponent(cityCropComponent);
    }

    @ReceiveEvent
    public void onFarmPlantGeneration(FarmPlantGenerationEvent event, EntityRef farm) {
        LocationComponent farmLocation = farm.getComponent(LocationComponent.class);

        for (int x = -event.plantableRadius; x <= event.plantableRadius; x++) {
            for (int y = -event.plantableRadius; y <= event.plantableRadius; y++) {
                if (Math.random() > event.genChance) {
                    continue;
                }

                EntityRef plantEntity = entityManager.create(event.plantName);
                Vector3i plantLocation = new Vector3i(farmLocation.getWorldPosition(new Vector3f()).add(x, PLANT_OFFSET, y), RoundingMode.FLOOR);

                plantEntity.send(new OnSeedPlanted(plantLocation));
            }
        }
    }
}
