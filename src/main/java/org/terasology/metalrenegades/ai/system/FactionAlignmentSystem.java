// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.metalrenegades.ai.system;

import org.terasology.assets.management.AssetManager;
import org.terasology.dynamicCities.settlements.events.SettlementRegisterEvent;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.metalrenegades.ai.component.FactionAlignmentComponent;
import org.terasology.registry.In;
import org.terasology.registry.Share;

/**
 * Assigns a faction to new settlements on spawn, and stores references to the character prefabs for all faction
 * types.
 */
@RegisterSystem(RegisterMode.AUTHORITY)
@Share(value = FactionAlignmentSystem.class)
public class FactionAlignmentSystem extends BaseComponentSystem {

    @In
    private AssetManager assetManager;

    @In
    private PrefabManager prefabManager;

    /**
     * Defines a particular alignment with an associated character prefab.
     */
    public enum Alignment {
            GOOD("goodCitizen"),
            BAD("badCitizen"),
        NEUTRAL("gooeyCitizen");

        private final String assetId;

        Alignment(String assetId) {
            this.assetId = assetId;
        }
    }

    /**
     * Returns the prefab associated with a provided faction alignment.
     *
     * @param alignment The alignment to return a prefab for.
     * @return The character prefab for this alignment.
     */
    public Prefab getPrefab(Alignment alignment) {
        return prefabManager.getPrefab(alignment.assetId);
    }

    @ReceiveEvent
    public void onSettlementRegisterEvent(SettlementRegisterEvent buildingEntitySpawnedEvent, EntityRef entityRef) {
        FactionAlignmentComponent factionAlignmentComponent = new FactionAlignmentComponent();
        factionAlignmentComponent.alignment = Alignment.values()[(int) (Math.random() * Alignment.values().length)];
        entityRef.addComponent(factionAlignmentComponent);
    }
}
