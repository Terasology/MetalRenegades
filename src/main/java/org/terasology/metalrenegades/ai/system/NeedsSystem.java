// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.metalrenegades.ai.system;

import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.entitySystem.prefab.PrefabManager;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.registry.In;
import org.terasology.engine.utilities.random.FastRandom;
import org.terasology.engine.utilities.random.Random;
import org.terasology.engine.world.time.WorldTimeEvent;
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;
import org.terasology.metalrenegades.ai.CitizenNeed;
import org.terasology.metalrenegades.ai.component.NeedsComponent;
import org.terasology.metalrenegades.ai.component.NeedsSystemConfigurationComponent;
import org.terasology.metalrenegades.ai.event.CitizenSpawnedEvent;

import java.util.Iterator;
import java.util.Optional;

/**
 * Manages needs for all citizens with {@link NeedsComponent}.
 */
@RegisterSystem(value = RegisterMode.AUTHORITY)
public class NeedsSystem extends BaseComponentSystem {

    @In
    private PrefabManager prefabManager;

    @In
    private EntityManager entityManager;

    private NeedsSystemConfigurationComponent configComponent;

    private Random random;

    @Override
    public void initialise() {
        random = new FastRandom();

        for (Prefab prefab : prefabManager.listPrefabs(NeedsSystemConfigurationComponent.class)) {
            NeedsSystemConfigurationComponent newConfig = prefab.getComponent(NeedsSystemConfigurationComponent.class);

            if (configComponent == null || configComponent.priority < newConfig.priority) {
                configComponent = newConfig;
            }
        }
    }

    @ReceiveEvent
    public void onCitizenSpawned(CitizenSpawnedEvent citizenSpawnedEvent, EntityRef citizen) {
        NeedsComponent needsComponent = new NeedsComponent();
        configComponent.needsConfigs.forEach(config -> {
            Iterator<String> it = config.iterator();

            CitizenNeed.Type needType = CitizenNeed.Type.valueOf(it.next());
            float capacity = getVariedFloat(Float.parseFloat(it.next()), Float.parseFloat(it.next()));
            float reductionRate = getVariedFloat(Float.parseFloat(it.next()), Float.parseFloat(it.next()));
            float goal = getVariedFloat(Float.parseFloat(it.next()), Float.parseFloat(it.next()));

            if (reductionRate > 0) {
                CitizenNeed newNeed = new CitizenNeed(needType, capacity, reductionRate, goal);
                needsComponent.needs.add(newNeed);
            }
        });

        citizen.saveComponent(needsComponent);
    }

    @ReceiveEvent
    public void onWorldTimeEvent(WorldTimeEvent worldTimeEvent, EntityRef entityRef) {
        for (EntityRef entity : entityManager.getEntitiesWith(NeedsComponent.class)) {
            NeedsComponent needsComponent = entity.getComponent(NeedsComponent.class);

            needsComponent.needs.stream().forEach(need -> {
                need.runNeedCycle();
            });

            entity.saveComponent(needsComponent);
        }
    }

    private float getVariedFloat(float initial, float variation) {
        return initial + variation * random.nextFloat(-1, 1);
    }

    public static CitizenNeed getNeedFromType(NeedsComponent needsComponent, CitizenNeed.Type needType) {
        Optional<CitizenNeed> validNeed = needsComponent.needs.stream()
                .filter(citizenNeed -> citizenNeed.getNeedType() == needType)
                .findFirst();

        return validNeed.orElse(null);
    }
}
