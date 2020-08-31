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
import org.terasology.logic.behavior.BehaviorAction;
import org.terasology.logic.behavior.core.Actor;
import org.terasology.logic.behavior.core.BaseAction;
import org.terasology.logic.behavior.core.BehaviorState;
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
