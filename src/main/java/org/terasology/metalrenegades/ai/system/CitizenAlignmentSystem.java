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

import org.lwjgl.opengl.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.management.AssetManager;
import org.terasology.dynamicCities.buildings.components.SettlementRefComponent;
import org.terasology.dynamicCities.construction.events.BuildingEntitySpawnedEvent;
import org.terasology.dynamicCities.settlements.events.SettlementRegisterEvent;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.metalrenegades.ai.component.FactionAlignmentComponent;
import org.terasology.metalrenegades.ai.component.HomeComponent;
import org.terasology.metalrenegades.ai.event.CitizenSpawnedEvent;
import org.terasology.registry.In;
import org.terasology.registry.Share;

@RegisterSystem(RegisterMode.AUTHORITY)
@Share(value = CitizenAlignmentSystem.class)
public class CitizenAlignmentSystem extends BaseComponentSystem {

    @In
    private AssetManager assetManager;

    @In
    private PrefabManager prefabManager;

    public enum Alignment {
            GOOD("goodCitizen"),
            BAD("badCitizen"),
        NEUTRAL("gooeyCitizen");

        private final String assetId;

        Alignment(String assetId) {
            this.assetId = assetId;
        }
    }

    public Prefab getPrefab(Alignment alignment) {
        return prefabManager.getPrefab(alignment.assetId);
    }

    @ReceiveEvent
    public void onSettlementRegisterEvent(SettlementRegisterEvent buildingEntitySpawnedEvent, EntityRef entityRef) {
        entityRef.addComponent(new FactionAlignmentComponent(Alignment.values()[(int) (Math.random() * Alignment.values().length)]));
    }

}
