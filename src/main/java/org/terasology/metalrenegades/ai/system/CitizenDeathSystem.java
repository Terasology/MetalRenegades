// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.metalrenegades.ai.system;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.EventPriority;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.health.BeforeDestroyEvent;
import org.terasology.metalrenegades.ai.component.CitizenComponent;
import org.terasology.metalrenegades.ai.event.CitizenDeathEvent;
import org.terasology.metalrenegades.minimap.events.RemoveCharacterFromOverlayEvent;

/**
 * System to handle Citizen Deaths within Metal Renegades
 */

@RegisterSystem(value = RegisterMode.AUTHORITY)
public class CitizenDeathSystem extends BaseComponentSystem {

    Logger logger = LoggerFactory.getLogger(CitizenDeathSystem.class);


    @ReceiveEvent(priority = EventPriority.PRIORITY_HIGH)
    public void onEntityDestroyed(BeforeDestroyEvent event, EntityRef entityRef,
                                  CitizenComponent citizenComponent) {
        entityRef.send(new CitizenDeathEvent());
        event.consume();
    }

    @ReceiveEvent
    public void onCitizenDeath(CitizenDeathEvent event, EntityRef entityRef) {
        entityRef.send(new RemoveCharacterFromOverlayEvent());
        logger.info("{} has died ", entityRef.toString());
        entityRef.destroy();

    }
}
