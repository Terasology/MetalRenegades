// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.metalrenegades.ai.actions;

import org.terasology.module.behaviors.components.FollowComponent;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.behavior.BehaviorAction;
import org.terasology.engine.logic.behavior.core.Actor;
import org.terasology.engine.logic.behavior.core.BaseAction;
import org.terasology.engine.logic.behavior.core.BehaviorState;
import org.terasology.engine.registry.In;
import org.terasology.metalrenegades.ai.component.SimpleSourceComponent;

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
