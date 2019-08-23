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
package org.terasology.metalrenegades.ai.system;

import com.google.common.collect.Lists;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.geom.Vector3f;
import org.terasology.metalrenegades.ai.component.CitizenComponent;
import org.terasology.metalrenegades.ai.component.FactionAlignmentComponent;
import org.terasology.metalrenegades.ai.component.NearbyCitizenEnemiesComponent;
import org.terasology.registry.In;

@RegisterSystem(value = RegisterMode.AUTHORITY)
public class FactionEnemiesSystem extends BaseComponentSystem implements UpdateSubscriberSystem {

    private static final float ENEMY_CHECK_DELAY = 5;

    private float counter;

    @In
    private EntityManager entityManager;

    @Override
    public void update(float delta) {
        counter+=delta;

        if(counter < ENEMY_CHECK_DELAY) {
            return;
        }

        counter = 0;

        for (EntityRef entity : entityManager.getEntitiesWith(NearbyCitizenEnemiesComponent.class)) {
            checkForEnemies(entity);
        }
    }

    private void checkForEnemies(EntityRef citizen) {
        if (!citizen.hasComponent(NearbyCitizenEnemiesComponent.class)
                || !citizen.hasComponent(FactionAlignmentComponent.class)) {
            return;
        }

        NearbyCitizenEnemiesComponent enemiesComponent = citizen.getComponent(NearbyCitizenEnemiesComponent.class);
        FactionAlignmentComponent alignmentComponent = citizen.getComponent(FactionAlignmentComponent.class);

        enemiesComponent.enemiesWithinRange = Lists.newArrayList();
        enemiesComponent.closestEnemy = EntityRef.NULL;

        float minDistance = Float.MAX_VALUE;
        Vector3f actorPosition = citizen.getComponent(LocationComponent.class).getWorldPosition();

        for (EntityRef otherCitizen : entityManager.getEntitiesWith(CitizenComponent.class)) {
            if (!otherCitizen.hasComponent(FactionAlignmentComponent.class)
                    || otherCitizen.equals(citizen)) {
                continue;
            }

            FactionAlignmentComponent otherAlignmentComponent = otherCitizen.getComponent(FactionAlignmentComponent.class);
            if (otherAlignmentComponent.alignment.equals(alignmentComponent.alignment) // continue if alignments are
                    || otherAlignmentComponent.alignment.equals(CitizenAlignmentSystem.Alignment.NEUTRAL) // the same, or either alignment
                    || alignmentComponent.alignment.equals(CitizenAlignmentSystem.Alignment.NEUTRAL)) { // is neutral.
                continue;
            }

            LocationComponent otherLocationComponent = otherCitizen.getComponent(LocationComponent.class);
            float distanceApart = otherLocationComponent.getWorldPosition().distanceSquared(actorPosition);

            if (distanceApart > enemiesComponent.searchRadius * enemiesComponent.searchRadius) {
                continue;
            }

            if (distanceApart < minDistance) {
                enemiesComponent.closestEnemy = otherCitizen;
                minDistance = otherLocationComponent.getWorldPosition().distanceSquared(actorPosition);
            }

            enemiesComponent.enemiesWithinRange.add(otherCitizen);
        }

        citizen.saveComponent(enemiesComponent);
    }
}