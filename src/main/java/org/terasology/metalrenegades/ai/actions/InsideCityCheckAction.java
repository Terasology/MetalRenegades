// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.metalrenegades.ai.actions;

import org.terasology.dynamicCities.settlements.SettlementEntityManager;
import org.terasology.logic.behavior.BehaviorAction;
import org.terasology.logic.behavior.core.Actor;
import org.terasology.logic.behavior.core.BaseAction;
import org.terasology.logic.behavior.core.BehaviorState;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.geom.Vector2i;
import org.terasology.math.geom.Vector3f;
import org.terasology.registry.CoreRegistry;
import org.terasology.registry.In;

/**
 * Checks if a character currently within the bounds of a city or not. Succeeds if the character is inside a city,
 * false otherwise.
 */
@BehaviorAction(name = "inside_city_check")
public class InsideCityCheckAction extends BaseAction {

    @In
    private transient SettlementEntityManager settlementEntityManager;

    @Override
    public void construct(Actor actor) {
        settlementEntityManager = CoreRegistry.get(SettlementEntityManager.class);
    }

    @Override
    public BehaviorState modify(Actor actor, BehaviorState result) {
        LocationComponent locationComponent = actor.getComponent(LocationComponent.class);
        Vector3f pos = locationComponent.getWorldPosition();

        if (settlementEntityManager.checkOutsideAllSettlements(new Vector2i(pos.x, pos.z))) {
            return BehaviorState.FAILURE;
        }

        return BehaviorState.SUCCESS;
    }

}
