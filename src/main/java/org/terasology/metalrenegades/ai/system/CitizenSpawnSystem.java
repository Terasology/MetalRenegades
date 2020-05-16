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

import org.terasology.dialogs.action.CloseDialogAction;
import org.terasology.dialogs.components.DialogComponent;
import org.terasology.dialogs.components.DialogPage;
import org.terasology.dialogs.components.DialogResponse;
import org.terasology.entitySystem.entity.EntityBuilder;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.inventory.events.GiveItemEvent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.metalrenegades.ai.CitizenNeed;
import org.terasology.metalrenegades.ai.component.CitizenComponent;
import org.terasology.metalrenegades.ai.component.HomeComponent;
import org.terasology.metalrenegades.ai.component.NeedsComponent;
import org.terasology.metalrenegades.ai.component.PotentialHomeComponent;
import org.terasology.metalrenegades.economy.MarketCitizenComponent;
import org.terasology.metalrenegades.economy.TraderComponent;
import org.terasology.metalrenegades.economy.actions.ShowTradingScreenAction;
import org.terasology.metalrenegades.minimap.events.AddCharacterToOverlayEvent;
import org.terasology.registry.In;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Spawns new citizens inside of available buildings with {@link PotentialHomeComponent}.
 */
@RegisterSystem(value = RegisterMode.AUTHORITY)
public class CitizenSpawnSystem extends BaseComponentSystem implements UpdateSubscriberSystem {

    private static final int SPAWN_CHECK_DELAY = 30;
    private static final int VERTICAL_SPAWN_OFFSET = 2;

    private float spawnTimer;

    @In
    private EntityManager entityManager;

    @In
    private PrefabManager prefabManager;

    @In
    private InventoryManager inventoryManager;

    @Override
    public void update(float delta) {
        spawnTimer += delta;

        if (spawnTimer > SPAWN_CHECK_DELAY) {
            for (EntityRef entity : entityManager.getEntitiesWith(PotentialHomeComponent.class)) {
                PotentialHomeComponent potentialHomeComponent = entity.getComponent(PotentialHomeComponent.class);
                if (potentialHomeComponent.citizens.size() >= potentialHomeComponent.maxCitizens) {
                    continue;
                }

                EntityRef citizen = spawnCitizen(entity);
                citizen.send(new AddCharacterToOverlayEvent());
                if (citizen == null) { // if no entity was generated.
                    continue;
                }

                potentialHomeComponent.citizens.add(citizen);

                entity.saveComponent(potentialHomeComponent);
            }

            spawnTimer = 0;
        }
    }

    /**
     * Spawns a random citizen inside the center of a provided building entity.
     *
     * @param homeEntity The building entity to spawn inside.
     * @return The new citizen entity, or null if spawning is not possible.
     */
    private EntityRef spawnCitizen(EntityRef homeEntity) {
        Prefab citizenPrefab = chooseCitizenPrefab();
        if (citizenPrefab == null) { // if no prefab is available.
            return null;
        }

        EntityBuilder entityBuilder = entityManager.newBuilder(chooseCitizenPrefab());

        LocationComponent homeLocationComponent = homeEntity.getComponent(LocationComponent.class);
        LocationComponent citizenLocationComponent = entityBuilder.getComponent(LocationComponent.class);
        HomeComponent homeComponent = new HomeComponent();

        homeComponent.building = homeEntity;
        citizenLocationComponent.setWorldPosition(homeLocationComponent.getWorldPosition().addY(VERTICAL_SPAWN_OFFSET));

        entityBuilder.addComponent(homeComponent);
        entityBuilder.saveComponent(citizenLocationComponent);

        NeedsComponent needsComponent = new NeedsComponent();

        needsComponent.needs.put(CitizenNeed.Type.FOOD, new CitizenNeed(20, 0.5f, 5, 15));
        needsComponent.needs.put(CitizenNeed.Type.WATER, new CitizenNeed(20, 1, 8, 20));
        needsComponent.needs.put(CitizenNeed.Type.SOCIAL, new CitizenNeed(30, 1, 15, 30));
        needsComponent.needs.put(CitizenNeed.Type.REST, new CitizenNeed(50, 0.5f, 20, 50));

        entityBuilder.saveComponent(needsComponent);

        EntityRef entityRef = entityBuilder.build();

        if (entityRef.hasComponent(TraderComponent.class)) {
            entityRef.addComponent(createTradeDialogComponent());
            setupStartInventory(entityRef);
        }

        return entityRef;
    }

    /**
     * Selects a random citizen prefab from a collection of prefabs with {@link CitizenComponent}.
     *
     * @return A random citizen prefab, or null if none are available.
     */
    private Prefab chooseCitizenPrefab() {
        Collection<Prefab> citizenList = prefabManager.listPrefabs(CitizenComponent.class);
        citizenList.removeIf(prefab -> prefab.hasComponent(MarketCitizenComponent.class));

        int i = (int) (Math.random() * citizenList.size());
        for (Prefab prefab : citizenList) {
            if (i-- <= 0) {
                return prefab;
            }
        }
        return null;
    }

    private void setupStartInventory(EntityRef citizen) {
        Prefab railgun = prefabManager.getPrefab("CoreAssets:gun");
        EntityRef item = entityManager.create(railgun);
        item.send(new GiveItemEvent(citizen));
    }

    private DialogComponent createTradeDialogComponent() {
        DialogComponent component = new DialogComponent();
        component.pages = new ArrayList<>();

        DialogPage page = new DialogPage();
        page.id = "main";
        page.title = "Wanna trade?";
        page.paragraphText = new ArrayList<>();
        page.responses = new ArrayList<>();

        page.paragraphText.add("I've got wares");

        DialogResponse tradeResponse = new DialogResponse();
        tradeResponse.text = "Show me what you got";
        tradeResponse.action = new ArrayList<>();
        tradeResponse.action.add(new ShowTradingScreenAction());

        DialogResponse closeResponse = new DialogResponse();
        closeResponse.text = "Later";
        closeResponse.action = new ArrayList<>();
        closeResponse.action.add(new CloseDialogAction());

        page.responses.add(tradeResponse);
        page.responses.add(closeResponse);

        component.pages.add(page);
        component.firstPage = page.id;

        return component;
    }
}
