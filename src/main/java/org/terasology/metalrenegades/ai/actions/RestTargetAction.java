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

import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.behavior.BehaviorAction;
import org.terasology.logic.behavior.core.Actor;
import org.terasology.logic.behavior.core.BaseAction;
import org.terasology.logic.behavior.core.BehaviorState;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.geom.Vector3f;
import org.terasology.metalrenegades.ai.component.RestSourceComponent;
import org.terasology.minion.move.MinionMoveComponent;
import org.terasology.registry.In;

/**
 * Sets this citizen's target to the nearest rest source, defined with a {@link RestSourceComponent}.
 */
@BehaviorAction(name = "set_target_rest_source")
public class RestTargetAction extends BaseAction {

    @In
    private EntityManager entityManager;

    @Override
    public BehaviorState modify(Actor actor, BehaviorState result) {
        Vector3f closestLocation = new Vector3f(300, 300, 300);
        Vector3f characterLocation = actor.getComponent(LocationComponent.class).getWorldPosition();

        for (EntityRef source : entityManager.getEntitiesWith(RestSourceComponent.class)) {
            LocationComponent sourceLocationComponent = source.getComponent(LocationComponent.class);
            if (sourceLocationComponent == null) {
                continue;
            }

            if (sourceLocationComponent.getWorldPosition().distanceSquared(characterLocation) < closestLocation.distanceSquared(characterLocation)) {
                closestLocation = sourceLocationComponent.getWorldPosition();
            }
        }

        if(closestLocation.equals(new Vector3f(300, 300, 300))) {
            return BehaviorState.FAILURE;
        }

        MinionMoveComponent minionMoveComponent = actor.getComponent(MinionMoveComponent.class);
        minionMoveComponent.target = closestLocation;
        actor.save(minionMoveComponent);

        return BehaviorState.SUCCESS;
    }

}
