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
package org.terasology.metalrenegades.minimap;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.metalrenegades.minimap.events.AddCharacterOverlayEvent;
import org.terasology.metalrenegades.minimap.events.RemoveCharacterOverlayEvent;
import org.terasology.dynamicCities.minimap.DistrictOverlay;
import org.terasology.dynamicCities.minimap.events.AddCentreOverlayEvent;
import org.terasology.dynamicCities.minimap.events.AddDistrictOverlayEvent;
import org.terasology.dynamicCities.minimap.events.RemoveDistrictOverlayEvent;
import org.terasology.dynamicCities.settlements.SettlementsCacheComponent;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.logic.players.MinimapSystem;
import org.terasology.metalrenegades.ai.component.CitizenComponent;
import org.terasology.network.ClientComponent;
import org.terasology.network.NetworkMode;
import org.terasology.network.NetworkSystem;
import org.terasology.registry.In;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@RegisterSystem(RegisterMode.CLIENT)
public class AddCharacterPositionOverlaySystem extends BaseComponentSystem {


    @In
    private MinimapSystem minimapSystem;

    @In
    private LocalPlayer localPlayer;

    @In
    private EntityManager entityManager;

    @In
    private NetworkSystem networkSystem;

    private Logger logger = LoggerFactory.getLogger(org.terasology.dynamicCities.minimap.MinimapOverlaySystem.class);

    private EntityRef clientEntity;

    private Map<EntityRef, Boolean> isOverlayAdded;

    private boolean isOverlaySinglePlayerAdded;

    @Override
    public void initialise() {
        if (networkSystem.getMode() == NetworkMode.CLIENT) {
            clientEntity = networkSystem.getServer().getClientEntity();
        }
        isOverlayAdded = new HashMap<>();

        Iterator<EntityRef> entities = entityManager.getEntitiesWith(CitizenComponent.class).iterator();
        while (entities.hasNext()) {
            minimapSystem.addOverlay(new AddCharacterOverlay(entities.next()));
        }

    }

    /**
     * Checks network constraints and adds the DistrictOverlay to all settlement entities
     * @param event
     * @param citizen
     */
    @ReceiveEvent
    public void onAddCharacterOverlayEvent(AddCharacterOverlayEvent event, EntityRef citizen) {

        logger.error("event has been handled");


            minimapSystem.addOverlay(new AddCharacterOverlay(citizen));

    }


    /**
     * Checks network constraints and adds the CentreOverlay to all settlement entities
     * @param event
     * @param entityRef
     */

    /**
     * Removes CharacterOverlay from the map
     * @param event
     * @param entityRef
     */
    @ReceiveEvent
    public void onRemoveCharacterOverlayEvent(RemoveCharacterOverlayEvent event, EntityRef entityRef) {
        if (networkSystem.getMode() == NetworkMode.CLIENT) {
            if (clientEntity.getComponent(ClientComponent.class).character.equals(entityRef.getId())) {
                if (isOverlayAdded.containsKey(entityRef)) {
                    isOverlayAdded.replace(entityRef, false);
                }
            }
        }
    }


}