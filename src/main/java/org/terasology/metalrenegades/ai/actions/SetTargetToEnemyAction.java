// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.metalrenegades.ai.actions;

import org.terasology.behaviors.components.FollowComponent;
import org.terasology.engine.logic.behavior.BehaviorAction;
import org.terasology.engine.logic.behavior.core.Actor;
import org.terasology.engine.logic.behavior.core.BaseAction;
import org.terasology.engine.logic.behavior.core.BehaviorState;
import org.terasology.metalrenegades.ai.component.NearbyCitizenEnemiesComponent;

/**
 * Action which sets this agent's move target to the nearest citizen from an enemy faction, as defined in {@link
 * NearbyCitizenEnemiesComponent}.
 */
@BehaviorAction(name = "set_target_to_enemy")
public class SetTargetToEnemyAction extends BaseAction {

    @Override
    public BehaviorState modify(Actor actor, BehaviorState result) {
        if (!actor.hasComponent(NearbyCitizenEnemiesComponent.class)) {
            return BehaviorState.SUCCESS;
        }

        NearbyCitizenEnemiesComponent enemiesComponent = actor.getComponent(NearbyCitizenEnemiesComponent.class);
        FollowComponent followComponent = new FollowComponent();

        followComponent.entityToFollow = enemiesComponent.closestEnemy;
        actor.getEntity().addComponent(followComponent);

        return BehaviorState.SUCCESS;
    }

}
