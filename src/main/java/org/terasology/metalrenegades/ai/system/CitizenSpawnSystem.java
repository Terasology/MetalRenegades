// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.metalrenegades.ai.system;

import org.joml.Vector3f;
import org.terasology.dialogs.action.CloseDialogAction;
import org.terasology.dialogs.components.DialogComponent;
import org.terasology.dialogs.components.DialogPage;
import org.terasology.dialogs.components.DialogResponse;
import org.terasology.dynamicCities.buildings.components.SettlementRefComponent;
import org.terasology.engine.entitySystem.entity.EntityBuilder;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.entitySystem.prefab.PrefabManager;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.engine.logic.inventory.events.GiveItemEvent;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.registry.In;
import org.terasology.metalrenegades.ai.component.FactionAlignmentComponent;
import org.terasology.metalrenegades.ai.component.HomeComponent;
import org.terasology.metalrenegades.ai.component.PotentialHomeComponent;
import org.terasology.metalrenegades.ai.event.CitizenSpawnedEvent;
import org.terasology.metalrenegades.economy.TraderComponent;
import org.terasology.metalrenegades.economy.actions.ShowTradingScreenAction;
import org.terasology.metalrenegades.minimap.events.AddCharacterToOverlayEvent;

import java.util.ArrayList;

/**
 * Spawns new citizens inside of available buildings with {@link PotentialHomeComponent}.
 */
@RegisterSystem(value = RegisterMode.AUTHORITY)
public class CitizenSpawnSystem extends BaseComponentSystem implements UpdateSubscriberSystem {

    private static final int SPAWN_CHECK_DELAY = 30;
    private static final int VERTICAL_SPAWN_OFFSET = 1;

    private float spawnTimer;

    @In
    private EntityManager entityManager;

    @In
    private InventoryManager inventoryManager;

    @In
    private FactionAlignmentSystem citizenAlignmentSystem;

    @In
    private PrefabManager prefabManager;

    @Override
    public void initialise() {
        // TODO: Temporary fix for injection malfunction in actions, ideally remove this in the future.
        citizenAlignmentSystem = CoreRegistry.get(FactionAlignmentSystem.class);
    }

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
        SettlementRefComponent settlementRefComponent = homeEntity.getComponent(SettlementRefComponent.class);
        FactionAlignmentComponent settlementAlignmentComponent = settlementRefComponent.settlement.getComponent(FactionAlignmentComponent.class);

        EntityBuilder entityBuilder = entityManager.newBuilder(citizenAlignmentSystem.getPrefab(settlementAlignmentComponent.alignment));

        LocationComponent homeLocationComponent = homeEntity.getComponent(LocationComponent.class);
        LocationComponent citizenLocationComponent = entityBuilder.getComponent(LocationComponent.class);
        HomeComponent homeComponent = new HomeComponent();

        homeComponent.building = homeEntity;
        citizenLocationComponent.setWorldPosition(homeLocationComponent.getWorldPosition(new Vector3f()).add(0, VERTICAL_SPAWN_OFFSET, 0));

        entityBuilder.addComponent(homeComponent);
        entityBuilder.saveComponent(citizenLocationComponent);
        entityBuilder.addComponent(new TraderComponent());

        EntityRef entityRef = entityBuilder.build();

        if (entityRef.hasComponent(TraderComponent.class)) {
            entityRef.addComponent(createTradeDialogComponent());
            setupStartInventory(entityRef);
        }

        entityRef.send(new CitizenSpawnedEvent());

        return entityRef;
    }

    private void setupStartInventory(EntityRef citizen) {
        Prefab railgun = prefabManager.getPrefab("CoreAdvancedAssets:dynamite");
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
