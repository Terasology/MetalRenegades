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
package org.terasology.metalrenegades.ai.actions;

import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.logic.behavior.BehaviorAction;
import org.terasology.logic.behavior.core.Actor;
import org.terasology.logic.behavior.core.BaseAction;
import org.terasology.logic.behavior.core.BehaviorState;
import org.terasology.metalrenegades.ai.CitizenNeed;
import org.terasology.metalrenegades.ai.component.SimpleSourceComponent;
import org.terasology.registry.In;

/**
 * Adds a social component to this citizen, indicating that it is ready for a social meeting.
 */
@BehaviorAction(name = "add_social_component")
public class AddSocialComponentAction extends BaseAction {

    @In
    private EntityManager entityManager;

    @Override
    public BehaviorState modify(Actor actor, BehaviorState result) {
        SimpleSourceComponent socialSourceComponent = new SimpleSourceComponent(CitizenNeed.Type.SOCIAL);
        actor.getEntity().addComponent(socialSourceComponent);

        return BehaviorState.SUCCESS;
    }

}
