// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.metalrenegades.ai.actions;

import org.terasology.engine.logic.behavior.BehaviorAction;
import org.terasology.engine.logic.behavior.core.Actor;
import org.terasology.engine.logic.behavior.core.BaseAction;
import org.terasology.engine.logic.behavior.core.BehaviorState;
import org.terasology.metalrenegades.ai.CitizenNeed;
import org.terasology.metalrenegades.ai.component.NeedsComponent;
import org.terasology.metalrenegades.ai.system.NeedsSystem;

/**
 * Checks the current status of a provided need type. Succeeds if action is needed to relieve this need, fails otherwise.
 */
@BehaviorAction(name = "check_need")
public class CheckNeedAction extends BaseAction {

    private String needType;

    @Override
    public BehaviorState modify(Actor actor, BehaviorState result) {
        if (!actor.hasComponent(NeedsComponent.class)) {
            return BehaviorState.FAILURE;
        }

        CitizenNeed.Type needTypeValue = CitizenNeed.Type.valueOf(needType);

        NeedsComponent needsComponent = actor.getComponent(NeedsComponent.class);
        CitizenNeed currentNeed = NeedsSystem.getNeedFromType(needsComponent, needTypeValue);

        if (currentNeed == null) {
            return BehaviorState.FAILURE;
        }

        return currentNeed.isBelowGoal() ? BehaviorState.SUCCESS : BehaviorState.FAILURE;
    }

}
