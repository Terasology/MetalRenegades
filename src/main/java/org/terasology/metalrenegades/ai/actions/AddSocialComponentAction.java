// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.metalrenegades.ai.actions;

import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.logic.behavior.BehaviorAction;
import org.terasology.engine.logic.behavior.core.Actor;
import org.terasology.engine.logic.behavior.core.BaseAction;
import org.terasology.engine.logic.behavior.core.BehaviorState;
import org.terasology.engine.registry.In;
import org.terasology.metalrenegades.ai.CitizenNeed;
import org.terasology.metalrenegades.ai.component.SimpleSourceComponent;

/**
 * Adds a social component to this citizen, indicating that it is ready for a social meeting.
 */
@BehaviorAction(name = "add_social_component")
public class AddSocialComponentAction extends BaseAction {

    @In
    private EntityManager entityManager;

    @Override
    public BehaviorState modify(Actor actor, BehaviorState result) {
        SimpleSourceComponent socialSourceComponent = new SimpleSourceComponent(CitizenNeed.Type.SOCIAL);
        actor.getEntity().addComponent(socialSourceComponent);

        return BehaviorState.SUCCESS;
    }

}
