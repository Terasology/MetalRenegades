// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.metalrenegades.ai.system;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.EventPriority;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.characters.AliveCharacterComponent;
import org.terasology.logic.characters.CharacterComponent;
import org.terasology.logic.characters.CharacterTeleportEvent;
import org.terasology.logic.health.BeforeDestroyEvent;
import org.terasology.logic.health.DestroyEvent;
import org.terasology.logic.health.HealthComponent;
import org.terasology.logic.health.event.RestoreFullHealthEvent;
import org.terasology.logic.players.PlayerCharacterComponent;
import org.terasology.metalrenegades.ai.event.CitizenDeathEvent;
import org.terasology.metalrenegades.minimap.events.RemoveCharacterFromOverlayEvent;
import org.terasology.registry.In;
import org.terasology.world.time.WorldTimeEvent;

/**
 * System to handle Citizen Deaths within Metal Renegades
 */

@RegisterSystem(value = RegisterMode.AUTHORITY)
public class CitizenDeathSystem extends BaseComponentSystem {

    Logger logger = LoggerFactory.getLogger(CitizenDeathSystem.class);


    @ReceiveEvent(priority = EventPriority.PRIORITY_HIGH)
    public void onCitizenDeath(BeforeDestroyEvent event, EntityRef entityRef,
                               AliveCharacterComponent aliveCharacterComponent) {
        entityRef.send(new RemoveCharacterFromOverlayEvent());

        logger.info("{} has died ", entityRef.toString());
        entityRef.destroy();

        event.consume();
    }

}
