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

import org.terasology.logic.behavior.BehaviorAction;
import org.terasology.logic.behavior.core.Actor;
import org.terasology.logic.behavior.core.BaseAction;
import org.terasology.logic.behavior.core.BehaviorState;
import org.terasology.metalrenegades.ai.CitizenNeed;
import org.terasology.metalrenegades.ai.component.NeedsComponent;

/**
 * Checks the current status of a provided need type. Succeeds if action is needed to relieve this need, fails otherwise.
 */
@BehaviorAction(name = "check_need")
public class CheckNeedAction extends BaseAction {

    private String needType;

    @Override
    public BehaviorState modify(Actor actor, BehaviorState result) {
        if(!actor.hasComponent(NeedsComponent.class)) {
            return BehaviorState.FAILURE;
        }

        CitizenNeed.Type needTypeValue = CitizenNeed.Type.valueOf(needType);

        NeedsComponent needsComponent = actor.getComponent(NeedsComponent.class);
        CitizenNeed currentNeed;

        switch (needTypeValue) {
            case FOOD:
                currentNeed = needsComponent.hungerNeed;
                break;
            case WATER:
                currentNeed = needsComponent.thirstNeed;
                break;
            case SOCIAL:
                currentNeed = needsComponent.socialNeed;
                break;
            case REST:
                currentNeed = needsComponent.restNeed;
                break;
            default:
                return BehaviorState.FAILURE;
        }

        return currentNeed.isBelowGoal() ? BehaviorState.SUCCESS : BehaviorState.FAILURE;
    }

}
