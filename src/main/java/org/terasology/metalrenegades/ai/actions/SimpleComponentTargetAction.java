// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.metalrenegades.ai.actions;

import org.joml.Vector3f;
import org.terasology.behaviors.components.FollowComponent;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.behavior.BehaviorAction;
import org.terasology.engine.logic.behavior.core.Actor;
import org.terasology.engine.logic.behavior.core.BaseAction;
import org.terasology.engine.logic.behavior.core.BehaviorState;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.registry.In;
import org.terasology.metalrenegades.ai.CitizenNeed;
import org.terasology.metalrenegades.ai.component.SimpleSourceComponent;

/**
 * Sets the character's follow target to a source which fulfills the specified need type.
 */
@BehaviorAction(name = "set_target_component")
public class SimpleComponentTargetAction extends BaseAction {

    @In
    private EntityManager entityManager;

    /**
     * The type of source that must be found, value set in the behavior tree.
     */
    private String needType;

    @Override
    public BehaviorState modify(Actor actor, BehaviorState result) {
        if (actor.getComponent(FollowComponent.class) != null) {
            return BehaviorState.SUCCESS;
        }
        CitizenNeed.Type needTypeValue = CitizenNeed.Type.valueOf(needType);

        float maxDistanceSquared = Float.MAX_VALUE;
        EntityRef closestSource = null;
        Vector3f characterLocation = actor.getComponent(LocationComponent.class).getWorldPosition(new Vector3f());

        for (EntityRef source : entityManager.getEntitiesWith(SimpleSourceComponent.class)) {
            LocationComponent sourceLocationComponent = source.getComponent(LocationComponent.class);
            if (sourceLocationComponent == null
                    || source.equals(actor.getEntity()) // needed for cases where this actor can itself be a source for other actors (social)
                    || !source.getComponent(SimpleSourceComponent.class).needType.equals(needTypeValue)) {
                continue;
            }

            float sourceDistanceSquared = sourceLocationComponent.getWorldPosition(new Vector3f()).distanceSquared(characterLocation);
            if (sourceDistanceSquared < maxDistanceSquared) {
                maxDistanceSquared = sourceDistanceSquared;
                closestSource = source;
            }
        }

        if (closestSource == null) {
            return BehaviorState.RUNNING;
        }

        FollowComponent followComponent = new FollowComponent();
        followComponent.entityToFollow = closestSource;
        actor.getEntity().addComponent(followComponent);

        return BehaviorState.SUCCESS;
    }

}
