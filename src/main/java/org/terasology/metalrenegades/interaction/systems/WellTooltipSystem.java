/*
 * Copyright 2020 MovingBlocks
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
package org.terasology.metalrenegades.interaction.systems;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.metalrenegades.ai.component.NeedsComponent;
import org.terasology.metalrenegades.interaction.component.WellSourceComponent;
import org.terasology.rendering.nui.layers.ingame.inventory.GetItemTooltip;
import org.terasology.rendering.nui.widgets.TooltipLine;
import org.terasology.wildAnimals.component.WildAnimalComponent;
import org.terasology.worldlyTooltipAPI.events.GetTooltipNameEvent;

/**
 * Adds tooltips to wells about the number of thirst refills available.
 */
@RegisterSystem(RegisterMode.CLIENT)
public class WellTooltipSystem extends BaseComponentSystem {

    @ReceiveEvent
    public void getTooltipName(GetTooltipNameEvent event, EntityRef entity, WildAnimalComponent wildAnimalComponent) {
        event.setName("Well Water");
    }

    @ReceiveEvent(components = WellSourceComponent.class)
    public void addRefillsToTooltip(GetItemTooltip event, EntityRef entity, NeedsComponent needsComponent) {
        WellSourceComponent wellSourceComp = entity.getComponent(WellSourceComponent.class);

        event.getTooltipLines().add(new TooltipLine("Refills: " + wellSourceComp.waterRefills + " / " + wellSourceComp.maxRefills));
    }

}
