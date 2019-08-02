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

import org.terasology.behaviors.components.FollowComponent;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.behavior.BehaviorAction;
import org.terasology.logic.behavior.core.Actor;
import org.terasology.logic.behavior.core.BaseAction;
import org.terasology.logic.behavior.core.BehaviorState;
import org.terasology.metalrenegades.ai.component.SimpleSourceComponent;
import org.terasology.registry.In;

/**
 * Prepares this citizen and the determined target citizen for a meeting.
 */
@BehaviorAction(name = "prepare_meetup")
public class PrepareSocialAction extends BaseAction {

    @In
    private EntityManager entityManager;

    @Override
    public BehaviorState modify(Actor actor, BehaviorState result) {
        EntityRef otherEntity = actor.getEntity().getComponent(FollowComponent.class).entityToFollow;
        otherEntity.removeComponent(SimpleSourceComponent.class);
        actor.getEntity().removeComponent(SimpleSourceComponent.class);

        FollowComponent otherFollowComponent = new FollowComponent();
        otherFollowComponent.entityToFollow = actor.getEntity();
        otherEntity.addComponent(otherFollowComponent);

        return BehaviorState.SUCCESS;
    }

}
