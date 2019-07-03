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

import org.terasology.entitySystem.entity.EntityBuilder;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.location.LocationComponent;
import org.terasology.metalrenegades.ai.component.CitizenComponent;
import org.terasology.metalrenegades.ai.component.HomeComponent;
import org.terasology.metalrenegades.ai.component.PotentialHomeComponent;
import org.terasology.registry.In;

import java.util.Collection;

/**
 * Spawns new citizens inside of available buildings with {@link PotentialHomeComponent}.
 */
@RegisterSystem(value = RegisterMode.AUTHORITY)
public class CitizenSpawnSystem extends BaseComponentSystem implements UpdateSubscriberSystem {

    private static final int SPAWN_CHECK_DELAY = 15;

    private float spawnTimer;

    @In
    private EntityManager entityManager;

    @In
    private PrefabManager prefabManager;

    @Override
    public void update(float delta) {
        spawnTimer += delta;

        if (spawnTimer > SPAWN_CHECK_DELAY) {
            for (EntityRef entity : entityManager.getEntitiesWith(PotentialHomeComponent.class)) {
                PotentialHomeComponent potentialHomeComponent = entity.getComponent(PotentialHomeComponent.class);
                if (potentialHomeComponent.citizens.size() >= potentialHomeComponent.maxCitizens) {
                    continue;
                }

                EntityRef citizen = spawnCitizen(entity);
                if (citizen == null) { // if no entity was generated.
                    continue;
                }

                potentialHomeComponent.citizens.add(citizen);

                entity.saveComponent(potentialHomeComponent);
            }

            spawnTimer = 0;
        }
    }

    /**
     * Spawns a random citizen inside the center of a provided building entity.
     *
     * @param homeEntity The building entity to spawn inside.
     * @return The new citizen entity, or null if spawning is not possible.
     */
    private EntityRef spawnCitizen(EntityRef homeEntity) {
        Prefab citizenPrefab = chooseCitizenPrefab();
        if (citizenPrefab == null) { // if no prefab is available.
            return null;
        }

        EntityBuilder entityBuilder = entityManager.newBuilder(chooseCitizenPrefab());

        LocationComponent homeLocationComponent = homeEntity.getComponent(LocationComponent.class);
        LocationComponent citizenLocationComponent = entityBuilder.getComponent(LocationComponent.class);
        HomeComponent homeComponent = new HomeComponent();

        homeComponent.building = homeEntity;
        citizenLocationComponent.setWorldPosition(homeLocationComponent.getWorldPosition());

        entityBuilder.addComponent(homeComponent);
        entityBuilder.saveComponent(citizenLocationComponent);

        return entityBuilder.build();
    }

    /**
     * Selects a random citizen prefab from a collection of prefabs with {@link CitizenComponent}.
     *
     * @return A random citizen prefab, or null if none are available.
     */
    private Prefab chooseCitizenPrefab() {
        Collection<Prefab> citizenList = prefabManager.listPrefabs(CitizenComponent.class);

        int i = (int) (Math.random() * citizenList.size());
        for (Prefab prefab: citizenList) {
            if (i-- <= 0) {
                return prefab;
            }
        }
        return null;
    }
}
