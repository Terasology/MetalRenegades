/*
 * Copyright 2019 MovingBlocks
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
package org.terasology.metalrenegades.quests;

import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.logic.players.MinimapSystem;
import org.terasology.network.ClientComponent;
import org.terasology.network.NetworkMode;
import org.terasology.network.NetworkSystem;
import org.terasology.registry.In;

/**
 * Manages the beacon minimap overlay
 */
@RegisterSystem(RegisterMode.CLIENT)
public class TaskOverlaySystem extends BaseComponentSystem {

    @In
    private NetworkSystem networkSystem;

    @In
    private EntityManager entityManager;

    @In
    private MinimapSystem minimapSystem;

    @In
    private LocalPlayer localPlayer;

    private TaskOverlay overlay;
    private EntityRef clientEntity;
    private boolean isOverlayAdded = false;

    @Override
    public void initialise() {
        if (networkSystem.getMode() == NetworkMode.CLIENT) {
            clientEntity = networkSystem.getServer().getClientEntity();
        }
    }

    @ReceiveEvent
    public void onAddBeaconOverlayEvent(AddBeaconOverlayEvent event, EntityRef character) {
        overlay = new TaskOverlay(event.beaconEntity);

        if (networkSystem.getMode() == NetworkMode.NONE) {
            minimapSystem.addOverlay(overlay);
            isOverlayAdded = true;
        }

        if (networkSystem.getMode() == NetworkMode.CLIENT) {
            if (clientEntity.getComponent(ClientComponent.class).character.getId() == character.getId() && !isOverlayAdded) {
                minimapSystem.addOverlay(overlay);
                isOverlayAdded = true;
            }
        }

        if (networkSystem.getMode() == NetworkMode.DEDICATED_SERVER && !isOverlayAdded) {
            if (localPlayer.getCharacterEntity() == character) {
                minimapSystem.addOverlay(overlay);
            }
        }
    }

    @ReceiveEvent
    public void onRemoveBeaconOverlayEvent(RemoveBeaconOverlayEvent event, EntityRef character) {
        minimapSystem.removeOverlay(overlay);
        isOverlayAdded = false;
        character.send(new DestroyActiveEntityEvent());
    }
}
