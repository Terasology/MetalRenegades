// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.metalrenegades.ai.actions;

import org.terasology.behaviors.components.FollowComponent;
import org.terasology.engine.logic.behavior.BehaviorAction;
import org.terasology.engine.logic.behavior.core.Actor;
import org.terasology.engine.logic.behavior.core.BaseAction;
import org.terasology.engine.logic.behavior.core.BehaviorState;
import org.terasology.metalrenegades.ai.CitizenNeed;
import org.terasology.metalrenegades.ai.component.NeedsComponent;
import org.terasology.metalrenegades.ai.system.NeedsSystem;

/**
 * Restores the need value of a provided need type.
 */
@BehaviorAction(name = "fulfill_need")
public class FulfillNeedAction extends BaseAction {

    private String needType;

    @Override
    public BehaviorState modify(Actor actor, BehaviorState result) {
        CitizenNeed.Type needTypeValue = CitizenNeed.Type.valueOf(needType);

        NeedsComponent needsComponent = actor.getComponent(NeedsComponent.class);
        CitizenNeed currentNeed = NeedsSystem.getNeedFromType(needsComponent, needTypeValue);
        if (currentNeed != null) {
            currentNeed.restoreNeed();
        }

        actor.save(needsComponent);
        actor.getEntity().removeComponent(FollowComponent.class);

        return BehaviorState.SUCCESS;
    }

}
