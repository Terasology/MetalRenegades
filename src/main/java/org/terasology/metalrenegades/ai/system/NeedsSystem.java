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
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.metalrenegades.ai.CitizenNeed;
import org.terasology.metalrenegades.ai.component.NeedsComponent;
import org.terasology.registry.In;

/**
 * Manages needs for all citizens with {@link NeedsComponent}.
 */
@RegisterSystem(value = RegisterMode.AUTHORITY)
public class NeedsSystem extends BaseComponentSystem implements UpdateSubscriberSystem {

    /**
     * Time in seconds required between need checks.
     */
    private static final int NEEDS_CHECK_DELAY = 30;

    /**
     * The time since last need check.
     */
    private float checkTimer;

    @In
    private EntityManager entityManager;

    @Override
    public void update(float delta) {
        checkTimer += delta;

        if (checkTimer > NEEDS_CHECK_DELAY) {
            for (EntityRef entity : entityManager.getEntitiesWith(NeedsComponent.class)) {
                NeedsComponent needsComponent = entity.getComponent(NeedsComponent.class);

                needsComponent.needs.forEach((k, v) -> v.runNeedCycle());

                entity.saveComponent(needsComponent);
            }

            checkTimer = 0;
        }
    }
}
