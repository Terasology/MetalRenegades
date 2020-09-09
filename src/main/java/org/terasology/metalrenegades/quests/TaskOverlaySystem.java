// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.metalrenegades.quests;

import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.network.ClientComponent;
import org.terasology.engine.network.NetworkMode;
import org.terasology.engine.network.NetworkSystem;
import org.terasology.engine.registry.In;
import org.terasology.minimap.logic.MinimapSystem;

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
