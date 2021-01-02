// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.metalrenegades.ai.actions;

import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.logic.behavior.BehaviorAction;
import org.terasology.logic.behavior.core.Actor;
import org.terasology.logic.behavior.core.BaseAction;
import org.terasology.logic.behavior.core.BehaviorState;
import org.terasology.metalrenegades.ai.CitizenNeed;
import org.terasology.metalrenegades.ai.component.SimpleSourceComponent;
import org.terasology.registry.In;

/**
 * Adds a social component to this citizen, indicating that it is ready for a social meeting.
 */
@BehaviorAction(name = "add_social_component")
public class AddSocialComponentAction extends BaseAction {

    @In
    private EntityManager entityManager;

    @Override
    public BehaviorState modify(Actor actor, BehaviorState result) {
        SimpleSourceComponent socialSourceComponent = new SimpleSourceComponent();
        socialSourceComponent.needType = CitizenNeed.Type.SOCIAL;
        actor.getEntity().addComponent(socialSourceComponent);

        return BehaviorState.SUCCESS;
    }
}
