// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.metalrenegades.ai.system;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.health.HealthComponent;
import org.terasology.metalrenegades.ai.event.CitizenDeathEvent;
import org.terasology.metalrenegades.minimap.events.RemoveCharacterFromOverlayEvent;
import org.terasology.registry.In;
import org.terasology.world.time.WorldTimeEvent;

@RegisterSystem(value = RegisterMode.AUTHORITY)
public class CitizenDeathSystem extends BaseComponentSystem {

    Logger logger = LoggerFactory.getLogger(CitizenDeathSystem.class);

    @In
    EntityManager entityManager;

    @ReceiveEvent
    public void onWorldTimeEvent(WorldTimeEvent event, EntityRef entityRef){
        for(EntityRef entity : entityManager.getEntitiesWith(HealthComponent.class)){
            if(entity.getComponent(HealthComponent.class).currentHealth<=0){

                entity.send(new CitizenDeathEvent());



            }
        }
    }

    @ReceiveEvent
    public void onCitizenDeath(CitizenDeathEvent event, EntityRef entity){
        logger.error("Remove character overlay has been called");
        entity.send(new RemoveCharacterFromOverlayEvent());
        logger.debug("Entity is destroyed  {}", entity.toFullDescription());
        //entity.destroy();
    }

}
