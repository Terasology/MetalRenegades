// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.metalrenegades.interaction.systems;

import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.nameTags.NameTagComponent;
import org.terasology.metalrenegades.interaction.component.WellSourceComponent;
import org.terasology.metalrenegades.interaction.events.CupFilledEvent;
import org.terasology.metalrenegades.interaction.events.WellDrinkEvent;
import org.terasology.metalrenegades.interaction.events.WellRefilledEvent;
import org.terasology.registry.In;
import org.terasology.rendering.nui.Color;
import org.terasology.world.time.WorldTimeEvent;

/**
 * Adds tooltips to wells about the number of thirst refills available.
 */
@RegisterSystem(value = RegisterMode.AUTHORITY)
public class WellTooltipSystem extends BaseComponentSystem {

    @In
    private EntityManager entityManager;

    @ReceiveEvent
    public void onWorldTimeEvent(WorldTimeEvent worldTimeEvent, EntityRef entityRef) {
        for (EntityRef waterSource : entityManager.getEntitiesWith(WellSourceComponent.class)) {
            if (waterSource.hasComponent(NameTagComponent.class)) {
                continue;
            }

            NameTagComponent nameTagComponent = new NameTagComponent();
            waterSource.addComponent(nameTagComponent);
            updateNameTag(waterSource);
        }
    }

    @ReceiveEvent(components = {WellSourceComponent.class, NameTagComponent.class})
    public void onCupFilled(CupFilledEvent cupFilledEvent, EntityRef wellEntity) {
        updateNameTag(wellEntity);
    }

    @ReceiveEvent(components = {WellSourceComponent.class, NameTagComponent.class})
    public void onWellDrink(WellDrinkEvent wellDrinkEvent, EntityRef wellEntity) {
        updateNameTag(wellEntity);
    }

    @ReceiveEvent(components = {WellSourceComponent.class, NameTagComponent.class})
    public void onWellRefill(WellRefilledEvent wellRefilledEvent, EntityRef wellEntity) {
        updateNameTag(wellEntity);
    }

    /**
     * Updates the name tag above a well entity to show the number of remaining water refills.
     *
     * @param wellEntity The well entity to update.
     */
    private void updateNameTag(EntityRef wellEntity) {
        WellSourceComponent wellSourceComponent = wellEntity.getComponent(WellSourceComponent.class);
        NameTagComponent nameTagComponent = wellEntity.getComponent(NameTagComponent.class);

        nameTagComponent.text = String.format("Drinks: %d/%d", wellSourceComponent.refillsLeft, wellSourceComponent.capacity);
        nameTagComponent.textColor = Color.BLUE;
        nameTagComponent.yOffset = 2f;
        nameTagComponent.scale = 1f;
        wellEntity.saveComponent(nameTagComponent);
    }

}
