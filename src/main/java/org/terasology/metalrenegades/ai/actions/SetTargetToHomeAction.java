// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.metalrenegades.ai.actions;

import org.joml.Vector3f;
import org.terasology.logic.behavior.BehaviorAction;
import org.terasology.logic.behavior.core.Actor;
import org.terasology.logic.behavior.core.BaseAction;
import org.terasology.logic.behavior.core.BehaviorState;
import org.terasology.logic.location.LocationComponent;
import org.terasology.metalrenegades.ai.component.HomeComponent;
import org.terasology.minion.move.MinionMoveComponent;

/**
 * Action which sets this agent's move target to the building entity defined in {@link HomeComponent}.
 */
@BehaviorAction(name = "set_target_to_home")
public class SetTargetToHomeAction extends BaseAction {

    @Override
    public BehaviorState modify(Actor actor, BehaviorState result) {
        HomeComponent homeComponent = actor.getComponent(HomeComponent.class);
        LocationComponent homeLocationComponent = homeComponent.building.getComponent(LocationComponent.class);

        Vector3f position = homeLocationComponent.getWorldPosition(new Vector3f());

        if (position != null) {
            MinionMoveComponent minionMoveComponent = actor.getComponent(MinionMoveComponent.class);
            minionMoveComponent.target = position;
            actor.save(minionMoveComponent);
        }

        return BehaviorState.SUCCESS;
    }

}
