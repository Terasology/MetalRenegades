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
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.location.LocationComponent;
import org.terasology.metalrenegades.ai.component.HomeComponent;
import org.terasology.registry.In;

@RegisterSystem(RegisterMode.AUTHORITY)
public class HomeSystem extends BaseComponentSystem implements UpdateSubscriberSystem {

    private static final Logger logger = LoggerFactory.getLogger(HomeSystem.class);

    @In
    private EntityManager entityManager;

    @Override
    public void update(float delta) {
        for (EntityRef entity : entityManager.getEntitiesWith(HomeComponent.class)) {
            HomeComponent homeComponent = entity.getComponent(HomeComponent.class);

            if(homeComponent.homePosition == null) {
                LocationComponent locationComponent = entity.getComponent(LocationComponent.class);

                homeComponent.homePosition = locationComponent.getWorldPosition();
                entity.saveComponent(homeComponent);
                logger.info("Home has been set");
            }
        }
    }

}
