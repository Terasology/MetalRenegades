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
package org.terasology.metalrenegades.ai.event;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.Event;
import org.terasology.protobuf.EntityData;

/**
 * Fired when a new citizen character is spawned by {@link org.terasology.metalrenegades.ai.system.CitizenSpawnSystem}.
 */
public class CitizenSpawnedEvent implements Event {


    // Stores the settlement to which the Citizen belongs to
    private EntityRef settlement;

    public CitizenSpawnedEvent(EntityRef settlement) {
        this.settlement = settlement;
    }

    public EntityRef getSettlement() {
        return this.settlement;
    }

}
