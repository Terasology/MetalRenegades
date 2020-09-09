// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.metalrenegades.ai.system;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.common.DisplayNameComponent;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.assets.texture.TextureRegionAsset;
import org.terasology.gestalt.assets.management.AssetManager;
import org.terasology.inventory.rendering.nui.layers.ingame.GetItemTooltip;
import org.terasology.metalrenegades.ai.component.CitizenComponent;
import org.terasology.metalrenegades.ai.component.NeedsComponent;
import org.terasology.nui.widgets.TooltipLine;
import org.terasology.worldlyTooltipAPI.events.GetTooltipIconEvent;
import org.terasology.worldlyTooltipAPI.events.GetTooltipNameEvent;

import java.util.Optional;

@RegisterSystem(RegisterMode.CLIENT)
public class CitizenTooltipSystem extends BaseComponentSystem {

    @In
    private AssetManager assetManager;

    @ReceiveEvent(components = CitizenComponent.class)
    public void getTooltipName(GetTooltipNameEvent event, EntityRef entity, CitizenComponent citizenComponent) {
        DisplayNameComponent displayNameComponent = entity.getComponent(DisplayNameComponent.class);
        event.setName(displayNameComponent.name);
    }

    @ReceiveEvent(components = NeedsComponent.class)
    public void addNeedsToTooltip(GetItemTooltip event, EntityRef entity, NeedsComponent needsComponent) {
        needsComponent.needs.stream()
                .forEach(need -> event.getTooltipLines()
                        .add(new TooltipLine(need.getNeedType().toString() + " " + need.toString())));
    }

    @ReceiveEvent(components = CitizenComponent.class)
    public void setIcon(GetTooltipIconEvent event, EntityRef entityRef, CitizenComponent citizenComponent) {
        Optional<TextureRegionAsset> iconAsset = assetManager.getAsset(entityRef.getParentPrefab().getName() + "Icon"
                , TextureRegionAsset.class);
        if (!iconAsset.isPresent()) {
            return;
        }

        event.setIcon(iconAsset.get());
    }

}
