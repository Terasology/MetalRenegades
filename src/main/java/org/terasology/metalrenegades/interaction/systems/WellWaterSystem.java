/*
 * Copyright 2018 MovingBlocks
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.dynamicCities.buildings.components.DynParcelRefComponent;
import org.terasology.dynamicCities.construction.events.BufferBlockEvent;
import org.terasology.dynamicCities.construction.events.BuildingEntitySpawnedEvent;
import org.terasology.dynamicCities.settlements.SettlementEntityManager;
import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.characters.CharacterHeldItemComponent;
import org.terasology.logic.common.ActivateEvent;
import org.terasology.logic.inventory.events.GiveItemEvent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector3i;
import org.terasology.metalrenegades.interaction.component.WaterCupComponent;
import org.terasology.metalrenegades.interaction.component.WellBlockComponent;
import org.terasology.metalrenegades.interaction.component.WellSourceComponent;
import org.terasology.metalrenegades.interaction.events.CupFilledEvent;
import org.terasology.metalrenegades.interaction.events.WellDrinkEvent;
import org.terasology.registry.In;
import org.terasology.thirst.component.ThirstComponent;
import org.terasology.thirst.event.DrinkConsumedEvent;
import org.terasology.world.time.WorldTimeEvent;

/**
 * Tracks water source blocks inside of wells, fills player's water cups upon interaction, and empties
 * water cups upon consumption.
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class WellWaterSystem extends BaseComponentSystem {

    private Logger logger = LoggerFactory.getLogger(WellWaterSystem.class);

    private static final int CYCLES_UNTIL_REFILL = 40;
    private int worldTimeCycles = 0;

    @In
    private EntityManager entityManager;

    @In
    private Time time;

    @ReceiveEvent
    public void onWorldTimeEvent(WorldTimeEvent worldTimeEvent, EntityRef entityRef) {
        if (worldTimeCycles >= CYCLES_UNTIL_REFILL) {
            for (EntityRef waterSource : entityManager.getEntitiesWith(WellSourceComponent.class)) {
                WellSourceComponent wellSourceComp = waterSource.getComponent(WellSourceComponent.class);
                wellSourceComp.waterRefills++;

                if (wellSourceComp.waterRefills > wellSourceComp.maxRefills) {
                    wellSourceComp.waterRefills = wellSourceComp.maxRefills;
                }
            }
            worldTimeCycles = 0;
        }

        worldTimeCycles++;
    }

    @ReceiveEvent(components = {WellBlockComponent.class})
    public void onActivate(ActivateEvent event, EntityRef sourceBlock) {
        EntityRef gatheringCharacter = event.getInstigator();
        EntityRef heldItem = gatheringCharacter.getComponent(CharacterHeldItemComponent.class).selectedItem;
        if (!gatheringCharacter.exists()) {
            return;
        }

        EntityRef wellEntity = getWellEntity(sourceBlock);
        if (wellEntity == null) {
            logger.warn("No well entity found for activated well block!");
            return;
        }

        WellSourceComponent wellSourceComp = wellEntity.getComponent(WellSourceComponent.class);
        if (wellSourceComp.waterRefills <= 0) { // if no refills remain, don't give water.
            return;
        }
        wellSourceComp.waterRefills--;
        wellEntity.saveComponent(wellSourceComp);

        if (!heldItem.hasComponent(WaterCupComponent.class)) {
            ThirstComponent thirst = gatheringCharacter.getComponent(ThirstComponent.class);

            thirst.lastCalculatedWater = thirst.maxWaterCapacity;
            thirst.lastCalculationTime = time.getGameTimeInMs();
            gatheringCharacter.saveComponent(thirst);

            wellEntity.send(new WellDrinkEvent(gatheringCharacter));

            return;
        }

        EntityRef cupItem = entityManager.create("MetalRenegades:waterCup");
        cupItem.send(new GiveItemEvent(gatheringCharacter));
        wellEntity.send(new CupFilledEvent(gatheringCharacter, cupItem));
        heldItem.destroy();
    }

    @ReceiveEvent(components = {WaterCupComponent.class})
    public void onDrinkConsumed(DrinkConsumedEvent event, EntityRef item) {
        EntityRef drinkingCharacter = event.getInstigator();
        EntityRef cupItem = entityManager.create("MetalRenegades:emptyCup");

        if (!cupItem.exists() || !drinkingCharacter.exists()) {
            return;
        }

        item.destroy();
        cupItem.send(new GiveItemEvent(drinkingCharacter));
    }

    /**
     * Retrieves the well entity associated with a well water source block.
     *
     * @param sourceBlock The activated source block.
     * @return The well entity that contains this block.
     */
    private EntityRef getWellEntity(EntityRef sourceBlock) {
        LocationComponent blockLocComp = sourceBlock.getComponent(LocationComponent.class);
        Vector3i blockLocation = new Vector3i(blockLocComp.getWorldPosition());
        EntityRef wellEntity = null;

        for (EntityRef waterSource : entityManager.getEntitiesWith(WellSourceComponent.class)) {
            DynParcelRefComponent dynParcelRefComponent = waterSource.getComponent(DynParcelRefComponent.class);
            Rect2i parcelRect = dynParcelRefComponent.dynParcel.getShape();

            if (parcelRect.contains(blockLocation.x, blockLocation.z)) {
                wellEntity = waterSource;
                break;
            }
        }

        return wellEntity;
    }

}
