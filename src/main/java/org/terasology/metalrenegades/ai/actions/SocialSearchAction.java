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
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.behavior.BehaviorAction;
import org.terasology.logic.behavior.core.Actor;
import org.terasology.logic.behavior.core.BaseAction;
import org.terasology.logic.behavior.core.BehaviorState;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.geom.Vector3f;
import org.terasology.metalrenegades.ai.CitizenNeed;
import org.terasology.metalrenegades.ai.component.NeedsComponent;
import org.terasology.registry.In;

/**
 * Searches for another citizen to meet up with, and initiates the meeting if one is found.
 */
@BehaviorAction(name = "search_social_partner")
public class SocialSearchAction extends BaseAction {

    @In
    private EntityManager entityManager;

    @Override
    public BehaviorState modify(Actor actor, BehaviorState result) {
        if(actor.hasComponent(FollowComponent.class)) { // if another character has already initiated a meeting
            return BehaviorState.SUCCESS;
        }

        Vector3f closestLocation = new Vector3f(1000, 1000, 1000);
        Vector3f characterLocation = actor.getComponent(LocationComponent.class).getWorldPosition();
        EntityRef otherCitizen = null;

        for (EntityRef other : entityManager.getEntitiesWith(NeedsComponent.class)) {
            NeedsComponent otherNeedsComponent = other.getComponent(NeedsComponent.class);
            LocationComponent otherLocationComponent = other.getComponent(LocationComponent.class);

            if (otherLocationComponent == null || otherNeedsComponent.needs.get(CitizenNeed.Type.SOCIAL).isBelowGoal() || other.hasComponent(FollowComponent.class)) {
                continue;
            }

            Vector3f otherLocation = otherLocationComponent.getWorldPosition();

            if ((otherLocation.distanceSquared(characterLocation) < closestLocation.distanceSquared(characterLocation)) && !actor.getEntity().equals(other))  {
                closestLocation = otherLocation;
                otherCitizen = other;
            }
        }

        if (otherCitizen == null) {
            return BehaviorState.FAILURE;
        }

        setupFollow(actor.getEntity(), otherCitizen);
        setupFollow(otherCitizen, actor.getEntity());

        return BehaviorState.SUCCESS;
    }

    /**
     * Sets the following target for one provided entity to the other entity.
     *
     * @param follower The entity that will follow the followee.
     * @param followee The entity that will be followed by the follower.
     */
    private void setupFollow(EntityRef follower, EntityRef followee) {
        FollowComponent followComponent = new FollowComponent();
        followComponent.entityToFollow = followee;

        follower.saveComponent(followComponent);
    }

}
