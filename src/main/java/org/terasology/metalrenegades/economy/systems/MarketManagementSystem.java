// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
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
import org.terasology.economy.components.PlayerResourceStoreComponent;
import org.terasology.economy.events.ResourceDrawEvent;
import org.terasology.economy.events.ResourceInfoRequestEvent;
import org.terasology.economy.events.ResourceStoreEvent;
import org.terasology.economy.events.SubscriberRegistrationEvent;
import org.terasology.economy.events.WalletTransactionEvent;
import org.terasology.economy.handler.MultiInvStorageHandler;
import org.terasology.economy.systems.MarketLogisticSystem;
import org.terasology.economy.systems.WalletAuthoritySystem;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.internal.EntityScope;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.inventory.ItemComponent;
import org.terasology.logic.players.event.OnPlayerSpawnedEvent;
import org.terasology.metalrenegades.economy.events.MarketTransactionRequest;
import org.terasology.metalrenegades.economy.events.TransactionType;
import org.terasology.metalrenegades.economy.ui.MarketItem;
import org.terasology.network.NetworkComponent;
import org.terasology.registry.In;
import org.terasology.registry.Share;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.entity.BlockCommands;
import org.terasology.world.block.items.BlockItemComponent;

import java.util.Set;

/**
 * Handles most core market features
 */
@Share(MarketManagementSystem.class)
@RegisterSystem(RegisterMode.AUTHORITY)
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
    private InventoryManager inventoryManager;

    @In
    private BlockCommands blockCommands;

    @In
    private BlockManager blockManager;

    @In
    private MultiInvStorageHandler handler;

    @In
    private WalletAuthoritySystem walletAuthoritySystem;

    private final int COOLDOWN = 200;
    private int counter = 0;

    private Logger logger = LoggerFactory.getLogger(MarketManagementSystem.class);

    @ReceiveEvent
    public void onPlayerJoin(OnPlayerSpawnedEvent onPlayerSpawnedEvent, EntityRef player) {
        EntityRef playerResourceStore = entityManager.create();
        playerResourceStore.addComponent(new InfiniteStorageComponent(1));
        playerResourceStore.setOwner(player);

        player.addComponent(new PlayerResourceStoreComponent(playerResourceStore));
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
        settlement.addComponent(new NetworkComponent());
        MarketComponent marketComponent = new MarketComponent(market);
        marketComponent.marketId = market.getId();
        settlement.addComponent(marketComponent);
    }

    @ReceiveEvent(components = MarketSubscriberComponent.class)
    public void onBuildingEntitySpawned(BuildingEntitySpawnedEvent event, EntityRef entityRef) {
        SettlementRefComponent settlementRefComponent = entityRef.getComponent(SettlementRefComponent.class);
        MarketComponent marketComponent = settlementRefComponent.settlement.getComponent(MarketComponent.class);
        entityRef.setScope(EntityScope.GLOBAL);
        MarketSubscriberComponent marketSubscriberComponent = entityRef.getComponent(MarketSubscriberComponent.class);
        marketSubscriberComponent.productStorage = marketComponent.market;
        marketSubscriberComponent.consumptionStorage = marketComponent.market;
        entityRef.saveComponent(marketSubscriberComponent);

        entityRef.send(new SubscriberRegistrationEvent());
    }

    /**
     * Initiate a transaction and delegate to an appropriate method depending
     * on the nature of the transaction
     */
    @ReceiveEvent
    public void onMarketTransactionRequest(MarketTransactionRequest request, EntityRef character) {
        if (request.type == TransactionType.BUYING) {
            buy(character, request.item);
        } else if (request.type == TransactionType.SELLING) {
            sell(character, request.item);
        } else {
            logger.warn("TransactionType invalid");
        }
    }

    private MarketItem buy(EntityRef character, MarketItem item) {
        if (!walletAuthoritySystem.isValidTransaction(character,-1 * item.cost)) {
            logger.warn("Insufficient funds");
            return item;
        } else if (item.quantity > 0) {

            if (!createItemOrBlock(character, item.name)) {
                logger.warn("Failed to create entity");
                return item;
            }

            SettlementRefComponent playerSettlementRef = character.getComponent(SettlementRefComponent.class);
            EntityRef playerResourceStore = character.getComponent(PlayerResourceStoreComponent.class).resourceStore;
            playerResourceStore.send(new ResourceDrawEvent(item.name, 1, playerSettlementRef.settlement.getComponent(MarketComponent.class).market));

            character.send(new WalletTransactionEvent(-1 * item.cost));
            item.quantity--;
        }

        return item;
    }

    private MarketItem sell(EntityRef character, MarketItem item) {
        if (item.quantity <=0 || !destroyItemOrBlock(character, item.name)) {
            logger.warn("Failed to destroy entity");
            return item;
        }
        EntityRef playerResourceStore = character.getComponent(PlayerResourceStoreComponent.class).resourceStore;
        SettlementRefComponent settlementRefComponent = character.getComponent(SettlementRefComponent.class);
        playerResourceStore.send(new ResourceStoreEvent(item.name, 1, settlementRefComponent.settlement.getComponent(MarketComponent.class).market));

        character.send(new WalletTransactionEvent(item.cost));
        item.quantity--;

        return item;
    }


    /**
     * Handles creating of actual inventory entity/block from the item bought
     * @param name Name of the item bought
     * @return Boolean indication whether the creating was a success or a failure
     */
    private boolean createItemOrBlock(EntityRef character, String name) {
        Set<ResourceUrn> matches = assetManager.resolve(name, Prefab.class);
        SettlementRefComponent playerSettlementRef = character.getComponent(SettlementRefComponent.class);
        ResourceInfoRequestEvent request = playerSettlementRef.settlement.getComponent(MarketComponent.class).market.send(new ResourceInfoRequestEvent());

        if (!request.isHandled || request.resources.get(name) <= 0) {
            return false;
        }

        if (matches.size() == 1) {
            Prefab prefab = assetManager.getAsset(matches.iterator().next(), Prefab.class).orElse(null);
            if (prefab != null && prefab.getComponent(ItemComponent.class) != null) {
                EntityRef entity = entityManager.create(prefab);
                if (!inventoryManager.giveItem(character, character, entity)) {
                    entity.destroy();
                    return true;
                }
            }
        }

        String blockURI = matches.iterator().next().getModuleName() + ":" + matches.iterator().next().getResourceName();
        String message = blockCommands.giveBlock(character.getOwner(), blockURI, 1, null);
        if (message != null) {
            return true;
        }

        return false;
    }

    /**
     * Handles removing entities/blocks from inventory when an item is sold
     * @param name Name of the item sold
     * @return Boolean indication whether the removal was a success or a failure
     */
    private boolean destroyItemOrBlock(EntityRef character, String name) {
        EntityRef item = EntityRef.NULL;
        try {
            for (int i = 0; i < inventoryManager.getNumSlots(character); i++) {
                EntityRef current = inventoryManager.getItemInSlot(character, i);

                if (EntityRef.NULL.equals(current)) {
                    continue;
                }

                if (name.equalsIgnoreCase(current.getParentPrefab().getName())) {
                    item = current;
                    break;
                }

                if (current.getParentPrefab().getName().equalsIgnoreCase("engine:blockItemBase")) {
                    if (current.getComponent(BlockItemComponent.class).blockFamily.getURI().toString().equalsIgnoreCase(name)) {
                        item = current;
                        break;
                    }
                }
            }
            if (item == EntityRef.NULL) {
                return false;
            }
            inventoryManager.removeItem(character, EntityRef.NULL, item, true, 1);
        } catch (Exception e) {
            logger.error("Could not create entity from {}. Exception: {}", name, e.getMessage());
            return false;
        }
        return true;
    }
}
