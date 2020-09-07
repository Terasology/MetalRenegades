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

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.gestalt.assets.management.AssetManager;
import org.terasology.logic.common.DisplayNameComponent;
import org.terasology.metalrenegades.ai.component.CitizenComponent;
import org.terasology.metalrenegades.ai.component.NeedsComponent;
import org.terasology.nui.widgets.TooltipLine;
import org.terasology.registry.In;
import org.terasology.rendering.assets.texture.TextureRegionAsset;
import org.terasology.rendering.nui.layers.ingame.inventory.GetItemTooltip;
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
        Optional<TextureRegionAsset> iconAsset = assetManager.getAsset(entityRef.getParentPrefab().getName() + "Icon", TextureRegionAsset.class);
        if (!iconAsset.isPresent()) {
            return;
        }

        event.setIcon(iconAsset.get());
    }

}
