/*
 * Copyright 2019 MovingBlocks
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
package org.terasology.metalrenegades.quests;

import org.terasology.dynamicCities.buildings.GenericBuildingComponent;
import org.terasology.dynamicCities.buildings.components.DynParcelRefComponent;
import org.terasology.dynamicCities.buildings.components.SettlementRefComponent;
import org.terasology.dynamicCities.construction.events.BuildingEntitySpawnedEvent;
import org.terasology.dynamicCities.parcels.DynParcel;
import org.terasology.economy.events.WalletTransactionEvent;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.nameTags.NameTagComponent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector3f;
import org.terasology.network.ClientComponent;
import org.terasology.registry.In;
import org.terasology.rendering.nui.Color;
import org.terasology.tasks.CollectBlocksTask;
import org.terasology.tasks.Task;
import org.terasology.tasks.TaskGraph;
import org.terasology.tasks.components.QuestComponent;
import org.terasology.tasks.components.QuestListComponent;
import org.terasology.tasks.components.QuestSourceComponent;
import org.terasology.tasks.events.BeforeQuestEvent;
import org.terasology.tasks.events.QuestCompleteEvent;
import org.terasology.tasks.events.StartTaskEvent;
import org.terasology.tasks.systems.QuestSystem;
import org.terasology.utilities.Assets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Manages the fetch meat quest
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class FetchQuestSystem extends BaseComponentSystem {

    @In
    private EntityManager entityManager;

    @In
    private InventoryManager inventoryManager;

    @In
    private QuestSystem questSystem;

    @In
    private LocalPlayer localPlayer;

    private EntityRef activeQuestEntity;
    private Map<String, Integer> amounts = new HashMap<>();

    private final String HOME_TASK_ID = "returnHome";
    private final String FETCH_QUEST_ID = "FetchQuest";
    private final String ITEM_ID = "WildAnimals:Meat";
    private final int REWARD = 50;

    @Override
    public void postBegin() {
        activeQuestEntity = EntityRef.NULL;
    }

    @ReceiveEvent(components = GenericBuildingComponent.class)
    public void onChurchSpawn(BuildingEntitySpawnedEvent event, EntityRef entityRef) {
        GenericBuildingComponent genericBuildingComponent = entityRef.getComponent(GenericBuildingComponent.class);
        if (genericBuildingComponent.name.equals("simplechurch")) {
            DynParcel dynParcel = entityRef.getComponent(DynParcelRefComponent.class).dynParcel;

            Optional<Prefab> questPointOptional = Assets.getPrefab("Tasks:QuestPoint");
            if (questPointOptional.isPresent()) {
                Rect2i rect2i = dynParcel.shape;
                Vector3f spawnPosition = new Vector3f(rect2i.minX() + rect2i.sizeX() / 2, dynParcel.getHeight() + 2, rect2i.minY() + rect2i.sizeY() / 2);
                EntityRef questPoint = entityManager.create(questPointOptional.get(), spawnPosition);
                SettlementRefComponent settlementRefComponent = entityRef.getComponent(SettlementRefComponent.class);
                questPoint.addComponent(settlementRefComponent);

                // Prepare the QuestListComponent
                QuestListComponent questListComponent = new QuestListComponent();
                questListComponent.questItems = new ArrayList<>();
                questListComponent.questItems.add("card");
                questPoint.addComponent(questListComponent);


                // Prepare the NameTagComponent
                NameTagComponent nameTagComponent = new NameTagComponent();
                nameTagComponent.text = "Quest";
                nameTagComponent.textColor = Color.YELLOW;
                nameTagComponent.scale = 2;
                nameTagComponent.yOffset = 2;
                questPoint.addComponent(nameTagComponent);


                // Prepare the LocationComponent
                LocationComponent locationComponent = new LocationComponent();
                locationComponent.setWorldPosition(spawnPosition);
                questPoint.addOrSaveComponent(locationComponent);
            }
        }
    }

    @ReceiveEvent
    public void onQuestActivated(BeforeQuestEvent event, EntityRef questItem) {
        activeQuestEntity = questItem.getComponent(QuestSourceComponent.class).source;

        QuestComponent questComponent = questItem.getComponent(QuestComponent.class);
        TaskGraph tasks = questComponent.tasks;
        for (Task t : tasks) {
            if (t instanceof CollectBlocksTask) {
                amounts.put(((CollectBlocksTask) t).getItemId(), ((CollectBlocksTask) t).getTargetAmount());
            }
        }
    }

    @ReceiveEvent
    public void onReturnTaskInitiated(StartTaskEvent event, EntityRef entityRef) {
        if (!Objects.equals(event.getQuest().getShortName(), FETCH_QUEST_ID)
                || !Objects.equals(event.getTask().getId(), HOME_TASK_ID)) {
            return;
        }

        LocationComponent locationComponent = activeQuestEntity.getComponent(LocationComponent.class);
        Optional<Prefab> beaconOptional = Assets.getPrefab("Tasks:BeaconMark");
        if (beaconOptional.isPresent()) {
            EntityRef beacon = entityManager.create(beaconOptional.get(), locationComponent.getWorldPosition());
            activeQuestEntity.destroy();
            activeQuestEntity = beacon;
        }

        localPlayer.getCharacterEntity().send(new AddBeaconOverlayEvent(activeQuestEntity));
    }

    @ReceiveEvent
    public void onQuestComplete(QuestCompleteEvent event, EntityRef client) {
        if (event.isSuccess()) {
            // Remove items from inventory
            ClientComponent component = client.getComponent(ClientComponent.class);
            EntityRef character = component.character;
            EntityRef item = EntityRef.NULL;

            for (int i = 0; i < inventoryManager.getNumSlots(character); i++) {
                EntityRef current = inventoryManager.getItemInSlot(character, i);
                if (!EntityRef.NULL.equals(current) && ITEM_ID.equalsIgnoreCase(current.getParentPrefab().getName())) {
                    item = current;
                    break;
                }
            }
            inventoryManager.removeItem(character, EntityRef.NULL, item, true, amounts.getOrDefault(ITEM_ID, 0));

            // Pay the player
            character.send(new WalletTransactionEvent(REWARD));

            // Remove the minmap overlay
            character.send(new RemoveBeaconOverlayEvent(activeQuestEntity));

            // remove the quest
            questSystem.removeQuest(event.getQuest(), true);

//            activeQuestEntity.destroy();
        }
    }

    @ReceiveEvent
    public void onDestroyActiveEntityEvent(DestroyActiveEntityEvent event, EntityRef character) {
        activeQuestEntity.destroy();
    }
}
