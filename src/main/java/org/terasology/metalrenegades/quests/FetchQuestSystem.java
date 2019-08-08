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
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector3f;
import org.terasology.metalrenegades.economy.systems.CurrencyManagementSystem;
import org.terasology.network.ClientComponent;
import org.terasology.protobuf.EntityData;
import org.terasology.registry.In;
import org.terasology.rendering.nui.Color;
import org.terasology.tasks.CollectBlocksTask;
import org.terasology.tasks.Task;
import org.terasology.tasks.components.QuestComponent;
import org.terasology.tasks.components.QuestListComponent;
import org.terasology.tasks.components.QuestSourceComponent;
import org.terasology.tasks.events.BeforeQuestEvent;
import org.terasology.tasks.events.QuestCompleteEvent;
import org.terasology.tasks.events.StartTaskEvent;
import org.terasology.utilities.Assets;
import sun.text.resources.et.FormatData_et;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@RegisterSystem(RegisterMode.AUTHORITY)
public class FetchQuestSystem extends BaseComponentSystem {

    @In
    private EntityManager entityManager;

    @In
    private CurrencyManagementSystem currencyManagementSystem;

    @In
    private InventoryManager inventoryManager;

    private EntityRef activeQuestEntity;
    private List<FetchedItem> fetchedItems = new ArrayList<>();

    private final String HOME_TASK_ID = "returnHome";
    private final String FETCH_QUEST_ID = "FetchQuest";
    private final int REWARD = 50;

    @Override
    public void postBegin() {
        activeQuestEntity = EntityRef.NULL;
    }

    @ReceiveEvent(components = GenericBuildingComponent.class)
    public void onMarketPlaceSpawn(BuildingEntitySpawnedEvent event, EntityRef entityRef) {
        GenericBuildingComponent genericBuildingComponent = entityRef.getComponent(GenericBuildingComponent.class);
        if (genericBuildingComponent.name.equals("marketplace")) {
            DynParcel dynParcel = entityRef.getComponent(DynParcelRefComponent.class).dynParcel;

            Optional<Prefab> questPointOptional = Assets.getPrefab("Tasks:QuestPoint");
            if (questPointOptional.isPresent()) {
                Rect2i rect2i = dynParcel.shape;
                Vector3f spawnPosition = new Vector3f(rect2i.minX() + rect2i.sizeX() / 2, dynParcel.getHeight() + 10, rect2i.minY() + rect2i.sizeY() / 2);
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
        List<Task> tasks = questComponent.tasks;
        fetchedItems = tasks.stream()
                .filter(task -> task instanceof CollectBlocksTask)
                .map(task -> new FetchedItem(
                        ((CollectBlocksTask) task).getItemId(),
                        ((CollectBlocksTask) task).getTargetAmount()
                )).collect(Collectors.toList());
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
    }

    @ReceiveEvent
    public void onQuestComplete(QuestCompleteEvent event, EntityRef client) {
        if (event.isSuccess()) {
            // Remove items from inventory
//            removeItems(client);

            // Destroy the beacon
            activeQuestEntity.destroy();

            // Pay the player
            currencyManagementSystem.changeWallet(REWARD);
        }
    }

    private void removeItems(EntityRef client) {
//        ClientComponent component = client.getComponent(ClientComponent.class);
//        EntityRef character = component.character;
//        EntityRef item = EntityRef.NULL;
//
//        try {
//            for (int i = 0; i < inventoryManager.getNumSlots(character); i++) {
//                EntityRef current = inventoryManager.getItemInSlot(character, i);
//                if (!EntityRef.NULL.equals(current)) {
//                    item = current;
//                    break;
//                }
//            }
//            inventoryManager.removeItem(localPlayer.getCharacterEntity(), EntityRef.NULL, item, true, 1);
//        } catch (Exception e) {
////            logger.error("Could not create entity from {}. Exception: {}", name, e.getMessage());
//        }
    }

    class FetchedItem {
        public String itemId;
        public int amount;

        public FetchedItem(String itemId, int amount) {
            this.itemId = itemId;
            this.amount = amount;
        }

        public FetchedItem() {
        }
    }
}
