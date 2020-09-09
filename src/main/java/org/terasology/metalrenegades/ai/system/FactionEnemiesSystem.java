// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.metalrenegades.ai.system;

import com.google.common.collect.Lists;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.registry.In;
import org.terasology.math.geom.Vector3f;
import org.terasology.metalrenegades.ai.component.CitizenComponent;
import org.terasology.metalrenegades.ai.component.FactionAlignmentComponent;
import org.terasology.metalrenegades.ai.component.NearbyCitizenEnemiesComponent;

/**
 * Tracks nearby faction enemies much like {@link org.terasology.behaviors.system.FindNearbyPlayersSystem}, and stores
 * the results in each citizens {@link NearbyCitizenEnemiesComponent}.
 */
@RegisterSystem(value = RegisterMode.AUTHORITY)
public class FactionEnemiesSystem extends BaseComponentSystem implements UpdateSubscriberSystem {

    private static final float ENEMY_CHECK_DELAY = 5;

    private float counter;

    @In
    private EntityManager entityManager;

    @Override
    public void update(float delta) {
        counter += delta;

        if (counter < ENEMY_CHECK_DELAY) {
            return;
        }

        counter = 0;

        for (EntityRef entity : entityManager.getEntitiesWith(NearbyCitizenEnemiesComponent.class)) {
            checkForEnemies(entity);
        }
    }

    /**
     * Checks for faction enemies nearby a particular citizen. Enemies are defined as follows: {@link
     * org.terasology.metalrenegades.ai.system.FactionAlignmentSystem.Alignment#GOOD} citizens are enemies of {@link
     * org.terasology.metalrenegades.ai.system.FactionAlignmentSystem.Alignment#BAD} citizens, and vice-versa, while
     * {@link org.terasology.metalrenegades.ai.system.FactionAlignmentSystem.Alignment#NEUTRAL} are enemies with no
     * other citizens.
     * <p>
     * Results are stored in {@link NearbyCitizenEnemiesComponent}.
     *
     * @param citizen The citizen to check enemies for.
     */
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

            FactionAlignmentComponent otherAlignmentComponent =
                    otherCitizen.getComponent(FactionAlignmentComponent.class);
            if (otherAlignmentComponent.alignment.equals(alignmentComponent.alignment) // continue if alignments are
                    || otherAlignmentComponent.alignment.equals(FactionAlignmentSystem.Alignment.NEUTRAL) // the 
                    // same, or either alignment
                    || alignmentComponent.alignment.equals(FactionAlignmentSystem.Alignment.NEUTRAL)) { // is neutral.
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
