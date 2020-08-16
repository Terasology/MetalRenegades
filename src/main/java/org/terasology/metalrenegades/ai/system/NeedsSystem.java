/*
 * Copyright 2018 MovingBlocks
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
package org.terasology.metalrenegades.ai.system;

import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.metalrenegades.ai.CitizenNeed;
import org.terasology.metalrenegades.ai.component.NeedsComponent;
import org.terasology.metalrenegades.ai.component.NeedsSystemConfigurationComponent;
import org.terasology.metalrenegades.ai.event.CitizenSpawnedEvent;
import org.terasology.registry.In;
import org.terasology.utilities.random.FastRandom;
import org.terasology.utilities.random.Random;
import org.terasology.world.time.WorldTimeEvent;

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
