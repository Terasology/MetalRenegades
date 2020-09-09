// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.metalrenegades.economy.actions;

import org.terasology.dialogs.ShowDialogEvent;
import org.terasology.dialogs.action.PlayerAction;
import org.terasology.engine.entitySystem.entity.EntityRef;

/**
 * Calls an event which would bring up a specified dialog page
 */
public class ShowDialogAction implements PlayerAction {


    private final String page;

    public ShowDialogAction(String page) {
        this.page = page;
    }

    @Override
    public void execute(EntityRef charEntity, EntityRef talkTo) {
        charEntity.send(new ShowDialogEvent(talkTo, page));
    }

    public String getPage() {
        return page;
    }


}

