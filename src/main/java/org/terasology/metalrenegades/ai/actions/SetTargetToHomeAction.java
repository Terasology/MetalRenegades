/*
 * Copyright 2019 MovingBlocks
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

import org.joml.Vector3f;
import org.terasology.logic.behavior.BehaviorAction;
import org.terasology.logic.behavior.core.Actor;
import org.terasology.logic.behavior.core.BaseAction;
import org.terasology.logic.behavior.core.BehaviorState;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.JomlUtil;
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
