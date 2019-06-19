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
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.metalrenegades.ai.component.TimeSensitiveComponent;
import org.terasology.registry.In;
import org.terasology.world.time.WorldTimeEvent;

@RegisterSystem(RegisterMode.AUTHORITY)
public class TimeSensitiveSystem extends BaseComponentSystem {

    @In
    private EntityManager entityManager;

    @ReceiveEvent
    public void onWorldTimeEvent(WorldTimeEvent worldTimeEvent, EntityRef entityRef) {
        for (EntityRef entity : entityManager.getEntitiesWith(TimeSensitiveComponent.class)) {
            TimeSensitiveComponent timeSensitiveComponent = entity.getComponent(TimeSensitiveComponent.class);
            timeSensitiveComponent.worldTime = worldTimeEvent.getWorldTime();
            timeSensitiveComponent.dayTime = timeSensitiveComponent.worldTime % 1;
            timeSensitiveComponent.isNight = (timeSensitiveComponent.worldTime % 1) < 0.2 || (timeSensitiveComponent.worldTime % 1) > 0.8;
            entity.saveComponent(timeSensitiveComponent);
        }
    }

}
