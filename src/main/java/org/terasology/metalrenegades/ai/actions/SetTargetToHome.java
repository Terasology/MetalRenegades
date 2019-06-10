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
import org.terasology.metalrenegades.ai.component.HomeComponent;
import org.terasology.minion.move.MinionMoveComponent;

@BehaviorAction(name = "set_target_to_home")
public class SetTargetToHome extends BaseAction {

    @Override
    public BehaviorState modify(Actor actor, BehaviorState result) {
        MinionMoveComponent moveComponent = actor.getComponent(MinionMoveComponent.class);
        HomeComponent homeComponent = actor.getComponent(HomeComponent.class);

        if(moveComponent.currentBlock == null || homeComponent.homePosition == null) {
            return BehaviorState.FAILURE;
        }

        moveComponent.target = homeComponent.homePosition;
        actor.save(moveComponent);

        return BehaviorState.SUCCESS;
    }

}
