// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
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
