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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.terasology.math.geom.Vector3f;
import org.terasology.metalrenegades.ai.component.AgentComponent;
import org.terasology.metalrenegades.ai.component.HomeComponent;
import org.terasology.metalrenegades.ai.component.PotentialHomeComponent;
import org.terasology.registry.In;

import java.util.Collection;

@RegisterSystem(value = RegisterMode.AUTHORITY)
public class AgentSpawnSystem extends BaseComponentSystem implements UpdateSubscriberSystem {

    private static final float SPAWN_DELAY_MINIMUM = 20;

    private static final Logger logger = LoggerFactory.getLogger(AgentSpawnSystem.class);

    @In
    private EntityManager entityManager;

    @In
    private PrefabManager prefabManager;

    @Override
    public void update(float delta) {
        for (EntityRef entity : entityManager.getEntitiesWith(PotentialHomeComponent.class)) {
            PotentialHomeComponent potentialHomeComponent = entity.getComponent(PotentialHomeComponent.class);
            if(potentialHomeComponent.character != null) {
                continue;
            }

            EntityRef agent = spawnAgent(entity);
            potentialHomeComponent.character = agent;

            entity.saveComponent(potentialHomeComponent);
        }
    }

    private EntityRef spawnAgent(EntityRef homeEntity) {
        EntityBuilder entityBuilder = entityManager.newBuilder(chooseAgentPrefab());

        LocationComponent homeLocationComponent = homeEntity.getComponent(LocationComponent.class);
        LocationComponent agentLocationComponent = entityBuilder.getComponent(LocationComponent.class);
        HomeComponent homeComponent = new HomeComponent();

        homeComponent.building = homeEntity;
        agentLocationComponent.setWorldPosition(homeLocationComponent.getWorldPosition());

        entityBuilder.addComponent(homeComponent);
        entityBuilder.saveComponent(agentLocationComponent);

        return entityBuilder.build();
    }

    private Prefab chooseAgentPrefab() {
        Collection<Prefab> agentList = prefabManager.listPrefabs(AgentComponent.class);

        int i = (int) (Math.random() * agentList.size());
        for (Prefab prefab: agentList) {
            if(i-- <= 0) {
                return prefab;
            }
        }
        return null;
    }
}
