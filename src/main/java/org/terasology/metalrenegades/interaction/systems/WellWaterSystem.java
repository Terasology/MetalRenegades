// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.metalrenegades.interaction.systems;

import org.joml.RoundingMode;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.terasology.dynamicCities.buildings.components.DynParcelRefComponent;
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
import org.terasology.math.JomlUtil;
import org.terasology.metalrenegades.interaction.component.WaterCupComponent;
import org.terasology.metalrenegades.interaction.component.WellBlockComponent;
import org.terasology.metalrenegades.interaction.component.WellSourceComponent;
import org.terasology.metalrenegades.interaction.events.CupFilledEvent;
import org.terasology.metalrenegades.interaction.events.WellDrinkEvent;
import org.terasology.metalrenegades.interaction.events.WellRefilledEvent;
import org.terasology.registry.In;
import org.terasology.thirst.component.ThirstComponent;
import org.terasology.thirst.event.DrinkConsumedEvent;
import org.terasology.world.block.BlockArea;
import org.terasology.world.time.WorldTimeEvent;

import java.util.stream.StreamSupport;

/**
 * Tracks water source blocks inside of wells, fills player's water cups upon interaction, and empties water cups upon
 * consumption.
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class WellWaterSystem extends BaseComponentSystem {

    @In
    private EntityManager entityManager;

    @In
    private Time time;

    /**
     * The number of world time cycles it takes for all wells to replenish with one refill.
     */
    private static final int CYCLES_UNTIL_REFILL = 20;

    private static final String EMPTY_CUP_URI = "MetalRenegades:emptyCup";

    private static final String FULL_CUP_URI = "MetalRenegades:filledCup";

    /**
     * A timer counting the current number of cycles until all wells are replenished.
     */
    private int worldTimeCycles = 0;

    @ReceiveEvent
    public void onWorldTimeEvent(WorldTimeEvent worldTimeEvent, EntityRef entityRef) {
        if (worldTimeCycles >= CYCLES_UNTIL_REFILL) {
            refillWells();
            worldTimeCycles = 0;
        }

        worldTimeCycles++;
    }

    @ReceiveEvent(components = {WellBlockComponent.class})
    public void onActivate(ActivateEvent event, EntityRef sourceBlock) {
        EntityRef gatheringCharacter = event.getInstigator();
        EntityRef wellEntity = getWellEntity(sourceBlock);
        if (!gatheringCharacter.exists() || wellEntity == EntityRef.NULL) {
            return;
        }

        WellSourceComponent wellSourceComp = wellEntity.getComponent(WellSourceComponent.class);
        boolean refillAvailable = useRefill(wellSourceComp);

        if (refillAvailable) {
            EntityRef heldItem = gatheringCharacter.getComponent(CharacterHeldItemComponent.class).selectedItem;

            if (heldItem.hasComponent(WaterCupComponent.class)) {
                fillWaterCup(gatheringCharacter, heldItem, wellEntity);
            } else {
                directDrink(gatheringCharacter, wellEntity);
            }
        }

        wellEntity.saveComponent(wellSourceComp);
    }

    @ReceiveEvent(components = {WaterCupComponent.class})
    public void onDrinkConsumed(DrinkConsumedEvent event, EntityRef item) {
        EntityRef drinkingCharacter = event.getInstigator();
        EntityRef cupItem = entityManager.create(EMPTY_CUP_URI);

        if (!cupItem.exists() || !drinkingCharacter.exists()) {
            return;
        }

        item.destroy(); // Replace the full cup with an empty cup.
        cupItem.send(new GiveItemEvent(drinkingCharacter));
    }

    /**
     * Fills a player's held empty cup with water. This is done by destroying the old cup entity, and giving the player
     * a new cup entity.
     *
     * @param gatheringCharacter The player entity that is collecting water.
     * @param oldCup The old cup entity that the player is holding.
     * @param wellEntity The well building entity that the water is supplied from.
     */
    private void fillWaterCup(EntityRef gatheringCharacter, EntityRef oldCup, EntityRef wellEntity) {
        EntityRef cupItem = entityManager.create(FULL_CUP_URI);
        oldCup.destroy();

        cupItem.send(new GiveItemEvent(gatheringCharacter));
        wellEntity.send(new CupFilledEvent(gatheringCharacter, cupItem));
    }

    /**
     * Replenishes a player's thirst bar back to full capacity.
     *
     * @param gatheringCharacter The player entity that is collecting water.
     * @param wellEntity The well building entity that the water is supplied from.
     */
    private void directDrink(EntityRef gatheringCharacter, EntityRef wellEntity) {
        ThirstComponent thirst = gatheringCharacter.getComponent(ThirstComponent.class);
        thirst.lastCalculatedWater = thirst.maxWaterCapacity;
        thirst.lastCalculationTime = time.getGameTimeInMs();
        gatheringCharacter.saveComponent(thirst);

        wellEntity.send(new WellDrinkEvent(gatheringCharacter));
    }

    /**
     * Replenish all wells in the game world with one refill.
     */
    private void refillWells() {
        for (EntityRef waterSource : entityManager.getEntitiesWith(WellSourceComponent.class)) {
            WellSourceComponent wellSourceComp = waterSource.getComponent(WellSourceComponent.class);
            if (addRefill(wellSourceComp)) {
                waterSource.send(new WellRefilledEvent());
            }
        }
    }

    /**
     * Retrieves the well entity associated with a well water source block.
     *
     * @param sourceBlock The activated source block.
     * @return The well entity that contains this block.
     */
    private EntityRef getWellEntity(EntityRef sourceBlock) {
        LocationComponent blockLocComp = sourceBlock.getComponent(LocationComponent.class);
        Vector3i blockLocation = new Vector3i(blockLocComp.getWorldPosition(new Vector3f()), RoundingMode.FLOOR);

        return StreamSupport.stream(entityManager.getEntitiesWith(WellSourceComponent.class).spliterator(), false)
                .filter(wellEntity -> buildingContainsPosition(wellEntity, blockLocation))
                .findFirst()
                .orElse(EntityRef.NULL);
    }

    /**
     * Checks if a building entity contains a particular world location. Used to determine which well entity a water
     * source block belongs to. The building area is considered infinite in the y-axis, only x and z coordinates are
     * checked.
     *
     * @param building The dynamic cities building entity to check.
     * @param location The world position that will be checked.
     * @return True if this building contains the location, false otherwise.
     */
    private boolean buildingContainsPosition(EntityRef building, Vector3i location) {
        DynParcelRefComponent dynParcelRefComponent = building.getComponent(DynParcelRefComponent.class);
        BlockArea parcelRect = new BlockArea(JomlUtil.from(dynParcelRefComponent.dynParcel.getShape().min()),
                JomlUtil.from(dynParcelRefComponent.dynParcel.getShape().max()));
        return parcelRect.contains(location.x, location.z);
    }

    /**
     * Attempts to use one refill from this well.
     *
     * @return True if this is successful, false if this well is empty.
     */
    public boolean useRefill(WellSourceComponent well) {
        if (well.refillsLeft <= 0) {
            return false;
        }
        well.refillsLeft--;
        return true;
    }

    /**
     * Attempts to add one refill to this well.
     *
     * @return True if this is successful, false if this well is already full.
     */
    public boolean addRefill(WellSourceComponent well) {
        if (well.refillsLeft >= well.capacity) {
            return false;
        }
        well.refillsLeft++;
        return true;
    }

}
