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

import com.google.common.collect.Lists;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.behavior.BehaviorAction;
import org.terasology.logic.behavior.core.Actor;
import org.terasology.logic.behavior.core.BaseAction;
import org.terasology.logic.behavior.core.BehaviorState;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.geom.Vector3f;
import org.terasology.metalrenegades.ai.component.CitizenComponent;
import org.terasology.metalrenegades.ai.component.FactionAlignmentComponent;
import org.terasology.metalrenegades.ai.component.NearbyCitizenEnemiesComponent;
import org.terasology.metalrenegades.ai.system.CitizenAlignmentSystem.Alignment;
import org.terasology.registry.In;

@BehaviorAction(name = "check_for_citizen_enemies")
public class CheckForCitizenEnemiesAction extends BaseAction {

    @In
    private EntityManager entityManager;

    @Override
    public BehaviorState modify(Actor actor, BehaviorState result) {
        if (!actor.hasComponent(NearbyCitizenEnemiesComponent.class)
                || !actor.hasComponent(FactionAlignmentComponent.class)) {
            return BehaviorState.SUCCESS;
        }

        NearbyCitizenEnemiesComponent enemiesComponent = actor.getComponent(NearbyCitizenEnemiesComponent.class);
        FactionAlignmentComponent alignmentComponent = actor.getComponent(FactionAlignmentComponent.class);

        enemiesComponent.enemiesWithinRange = Lists.newArrayList();
        enemiesComponent.closestEnemy = EntityRef.NULL;

        float minDistance = Float.MAX_VALUE;
        Vector3f actorPosition = actor.getComponent(LocationComponent.class).getWorldPosition();

        for (EntityRef citizen : entityManager.getEntitiesWith(CitizenComponent.class)) {
            if(!citizen.hasComponent(FactionAlignmentComponent.class)
                    || citizen.equals(actor.getEntity())) {
                continue;
            }

            FactionAlignmentComponent citizenAlignmentComponent = citizen.getComponent(FactionAlignmentComponent.class);
            if (citizenAlignmentComponent.alignment.equals(alignmentComponent.alignment) // continue if alignments are
                    || citizenAlignmentComponent.alignment.equals(Alignment.NEUTRAL) // the same, or either alignment
                    || alignmentComponent.alignment.equals(Alignment.NEUTRAL)) { // is neutral.
                continue;
            }

            LocationComponent citizenLocationComponent = citizen.getComponent(LocationComponent.class);
            float distanceApart = citizenLocationComponent.getWorldPosition().distanceSquared(actorPosition);

            if(distanceApart > enemiesComponent.searchRadius) {
                continue;
            }

            if (distanceApart < minDistance) {
                enemiesComponent.closestEnemy = citizen;
                minDistance = citizenLocationComponent.getWorldPosition().distanceSquared(actorPosition);
            }

            enemiesComponent.enemiesWithinRange.add(citizen);
        }

        return BehaviorState.SUCCESS;
    }

}
