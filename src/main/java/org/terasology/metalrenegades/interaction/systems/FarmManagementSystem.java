/*
 * Copyright 2020 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.metalrenegades.interaction.systems;

import org.terasology.dynamicCities.buildings.components.SettlementRefComponent;
import org.terasology.dynamicCities.settlements.events.SettlementRegisterEvent;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.geom.Vector3i;
import org.terasology.metalrenegades.interaction.component.CityCropComponent;
import org.terasology.metalrenegades.interaction.component.FarmComponent;
import org.terasology.metalrenegades.interaction.event.FarmPlantGenerationEvent;
import org.terasology.registry.In;
import org.terasology.simpleFarming.events.OnSeedPlanted;
import org.terasology.world.time.WorldTimeEvent;

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

        CityCropComponent cityCropComponent = new CityCropComponent(cityCrop[0], cityCrop[1]);
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
                Vector3i plantLocation = new Vector3i(farmLocation.getWorldPosition().add(x, PLANT_OFFSET, y));

                plantEntity.send(new OnSeedPlanted(plantLocation));
            }
        }
    }

}
