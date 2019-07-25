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
package org.terasology.metalrenegades.economy.systems;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.ResourceUrn;
import org.terasology.assets.management.AssetManager;
import org.terasology.dynamicCities.buildings.components.SettlementRefComponent;
import org.terasology.dynamicCities.construction.events.BuildingEntitySpawnedEvent;
import org.terasology.dynamicCities.playerTracking.PlayerTracker;
import org.terasology.dynamicCities.population.CultureComponent;
import org.terasology.dynamicCities.population.PopulationComponent;
import org.terasology.dynamicCities.settlements.components.ActiveSettlementComponent;
import org.terasology.dynamicCities.settlements.components.MarketComponent;
import org.terasology.dynamicCities.settlements.events.SettlementRegisterEvent;
import org.terasology.economy.components.InfiniteStorageComponent;
import org.terasology.economy.components.MarketSubscriberComponent;
import org.terasology.economy.components.MultiInvStorageComponent;
import org.terasology.economy.events.ResourceDrawEvent;
import org.terasology.economy.events.ResourceInfoRequestEvent;
import org.terasology.economy.events.ResourceStoreEvent;
import org.terasology.economy.events.SubscriberRegistrationEvent;
import org.terasology.economy.handler.MultiInvStorageHandler;
import org.terasology.economy.systems.MarketLogisticSystem;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.inventory.ItemComponent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.metalrenegades.economy.events.TransactionType;
import org.terasology.metalrenegades.economy.ui.MarketItem;
import org.terasology.network.ClientComponent;
import org.terasology.registry.In;
import org.terasology.registry.Share;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.entity.BlockCommands;

import java.util.Set;

@Share(MarketManagementSystem.class)
@RegisterSystem
public class MarketManagementSystem extends BaseComponentSystem implements UpdateSubscriberSystem {

    @In
    private EntityManager entityManager;

    @In
    private MarketLogisticSystem marketLogisticSystem;

    @In
    private AssetManager assetManager;

    @In
    private PlayerTracker playerTracker;

    @In
    private CurrencyManagementSystem currencySystem;

    @In
    private InventoryManager inventoryManager;

    @In
    private LocalPlayer localPlayer;

    @In
    private BlockCommands blockCommands;

    @In
    private BlockManager blockManager;

    @In
    private MultiInvStorageHandler handler;

    private EntityRef playerResourceStore;

    private final int COOLDOWN = 200;
    private int counter = 0;

    private Logger logger = LoggerFactory.getLogger(MarketManagementSystem.class);

    @Override
    public void postBegin() {
        playerResourceStore = entityManager.create();
        playerResourceStore.addComponent(new InfiniteStorageComponent(1));
    }

    @Override
    public void update(float delta) {
        if (counter != 0) {
            counter--;
            return;
        }

        Iterable<EntityRef> bldgsWithChests = entityManager.getEntitiesWith(MultiInvStorageComponent.class, SettlementRefComponent.class);
        for (EntityRef bldg : bldgsWithChests) {
            if (!bldg.isActive() || !bldg.exists()) {
                continue;
            }
            ResourceInfoRequestEvent requestEvent = new ResourceInfoRequestEvent();
            bldg.send(requestEvent);
            SettlementRefComponent settlementRefComponent = bldg.getComponent(SettlementRefComponent.class);
            if (requestEvent.isHandled && !requestEvent.resources.isEmpty()) {
                for (String resource : requestEvent.resources.keySet()) {
                    if (requestEvent.resources.get(resource) != 0) {
                        logger.info("Storing resources in the market...");
                        bldg.send(new ResourceStoreEvent(resource, requestEvent.resources.get(resource), settlementRefComponent.settlement.getComponent(MarketComponent.class).market));
                    }
                }
            }
        }
        counter = COOLDOWN;
    }

    @ReceiveEvent(components = {ActiveSettlementComponent.class, PopulationComponent.class, CultureComponent.class})
    public void onSettlementSpawnEvent(SettlementRegisterEvent event, EntityRef settlement) {
        EntityRef market = entityManager.create(new InfiniteStorageComponent(1));
        MarketComponent marketComponent = new MarketComponent(market);
        settlement.addComponent(marketComponent);
    }

    @ReceiveEvent(components = MarketSubscriberComponent.class)
    public void onBuildingEntitySpawned(BuildingEntitySpawnedEvent event, EntityRef entityRef) {
        SettlementRefComponent settlementRefComponent = entityRef.getComponent(SettlementRefComponent.class);
        MarketComponent marketComponent = settlementRefComponent.settlement.getComponent(MarketComponent.class);
        entityRef.setAlwaysRelevant(true);
        MarketSubscriberComponent marketSubscriberComponent = entityRef.getComponent(MarketSubscriberComponent.class);
        marketSubscriberComponent.productStorage = marketComponent.market;
        marketSubscriberComponent.consumptionStorage = marketComponent.market;
        entityRef.saveComponent(marketSubscriberComponent);

        entityRef.send(new SubscriberRegistrationEvent());
    }

    // TODO: Expose transaction logic as events
//    @ReceiveEvent
//    public void onMarketTransactionConfirm(MarketTransactionEvent event, EntityRef character) {
//
//    }

    public MarketItem handleTransaction(MarketItem item, TransactionType type) {
        if (type == TransactionType.BUYING) {
            return buy(item);
        } else if (type == TransactionType.SELLING) {
            return sell(item);
        } else {
            logger.warn("TransactionType invalid");
            return item;
        }
    }

    private MarketItem buy(MarketItem item) {
        if (!currencySystem.isValidTransaction(-1 * item.cost)) {
            logger.warn("Insufficient funds");
            return item;
        } else {

            if (!createItemOrBlock(item.name)) {
                logger.warn("Failed to create entity");
                return item;
            }

            Iterable<EntityRef> storageBuildings = entityManager.getEntitiesWith(MultiInvStorageComponent.class, SettlementRefComponent.class);
            for (EntityRef bldg : storageBuildings) {
                if (!bldg.isActive() || !bldg.exists()) {
                    continue;
                }

                MultiInvStorageComponent component = bldg.getComponent(MultiInvStorageComponent.class);

                playerResourceStore.send(new ResourceDrawEvent(item.name, 1, bldg));

                logger.info("\n\n playerStore: {} \n bldg: {} \n\n",
                        playerResourceStore.getComponent(InfiniteStorageComponent.class).inventory.get(item.name),
                        handler.availableResourceAmount(component, item.name));

                item.quantity--;
                break;
            }

            currencySystem.changeWallet(-1 * item.cost);
        }

        return item;
    }

    private MarketItem sell(MarketItem item) {
        if (item.quantity <=0 || !destroyItemOrBlock(item.name)) {
            logger.warn("Failed to create entity");
            return item;
        }

        Iterable<EntityRef> storageBuildings = entityManager.getEntitiesWith(MultiInvStorageComponent.class, SettlementRefComponent.class);
        for (EntityRef bldg : storageBuildings) {
            if (!bldg.isActive() || !bldg.exists()) {
                continue;
            }

            SettlementRefComponent settlementRefComponent = bldg.getComponent(SettlementRefComponent.class);
            bldg.send(new ResourceStoreEvent(item.name, 1, settlementRefComponent.settlement.getComponent(MarketComponent.class).market));
            item.quantity--;
            break;
        }

        currencySystem.changeWallet(item.cost);
        return item;
    }


    private boolean createItemOrBlock(String name) {
        Set<ResourceUrn> matches = assetManager.resolve(name, Prefab.class);

        if (matches.size() == 1) {
            Prefab prefab = assetManager.getAsset(matches.iterator().next(), Prefab.class).orElse(null);
            if (prefab != null && prefab.getComponent(ItemComponent.class) != null) {
                EntityRef playerEntity = localPlayer.getClientEntity().getComponent(ClientComponent.class).character;
                EntityRef entity = entityManager.create(prefab);
                if (!inventoryManager.giveItem(playerEntity, playerEntity, entity)) {
                    entity.destroy();
                    return true;
                }
            }
        }

        String message = blockCommands.giveBlock(localPlayer.getClientEntity(), name, 1, null);
        if (message != null) {
            return true;
        }

        return false;
    }

    private boolean destroyItemOrBlock(String name) {
        EntityRef player = localPlayer.getCharacterEntity();
        EntityRef item = EntityRef.NULL;
        try {
            for (int i = 0; i < inventoryManager.getNumSlots(player); i++) {
                EntityRef current = inventoryManager.getItemInSlot(player, i);
                if (!EntityRef.NULL.equals(current) && name.equalsIgnoreCase(current.getParentPrefab().getName())) {
                    item = current;
                    break;
                }
            }
            inventoryManager.removeItem(localPlayer.getCharacterEntity(), EntityRef.NULL, item, true, 1);
        } catch (Exception e) {
            logger.error("Could not create entity from {}. Exception: {}", name, e.getMessage());
            return false;
        }
        return true;
    }
}
