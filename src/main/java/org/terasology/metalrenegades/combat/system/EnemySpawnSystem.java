// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.metalrenegades.combat.system;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import org.terasology.behaviors.system.NightTrackerSystem;
import org.terasology.dynamicCities.buildings.components.SettlementRefComponent;
import org.terasology.dynamicCities.settlements.SettlementEntityManager;
import org.terasology.entitySystem.entity.EntityBuilder;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.players.event.OnPlayerRespawnedEvent;
import org.terasology.math.geom.Vector2i;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.metalrenegades.combat.component.EnemyGracePeriodComponent;
import org.terasology.metalrenegades.combat.component.NightEnemyComponent;
import org.terasology.network.ClientComponent;
import org.terasology.registry.CoreRegistry;
import org.terasology.registry.In;
import org.terasology.utilities.random.FastRandom;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.sun.OnDawnEvent;

import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.function.Function;

@RegisterSystem(RegisterMode.AUTHORITY)
public class EnemySpawnSystem extends BaseComponentSystem implements UpdateSubscriberSystem {

    @In
    private EntityManager entityManager;

    @In
    private WorldProvider worldProvider;

    @In
    private BlockManager blockManager;

    @In
    private NightTrackerSystem nightTrackerSystem;

    @In
    private SettlementEntityManager settlementEntityManager;

    /**
     * Check blocks at and around the target position and check if it's a valid spawning spot
     */
    private Function<Vector3i, Boolean> isValidSpawnPosition;

    private FastRandom random;

    private Block sand;
    private Block air;

    private int cyclesLeft;

    private Queue<EntityRef> enemyQueue;

    private static final int MAX_ENEMIES = 20;

    private boolean ready;

    @Override
    public void postBegin() {
        sand = blockManager.getBlock("CoreAssets:Sand");
        air = blockManager.getBlock(BlockManager.AIR_ID);

        random = new FastRandom();
        isValidSpawnPosition = this::isValidSpawnPosition;

        nightTrackerSystem = CoreRegistry.get(NightTrackerSystem.class);
        enemyQueue = Queues.newLinkedBlockingQueue();
        ready = true;
    }

    @Override
    public void update(float delta) {
        if (cyclesLeft < 300 || !nightTrackerSystem.isNight() || !ready) {
            cyclesLeft++;
            return;
        }
        cyclesLeft = 0;

        for (EntityRef client : entityManager.getEntitiesWith(ClientComponent.class)) {
            ClientComponent clientComponent = client.getComponent(ClientComponent.class);
            EntityRef character = clientComponent.character;

            if (character.hasComponent(EnemyGracePeriodComponent.class)) {
                EnemyGracePeriodComponent enemyGracePeriodComponent =
                        character.getComponent(EnemyGracePeriodComponent.class);
                enemyGracePeriodComponent.cyclesLeft--;

                if (enemyGracePeriodComponent.cyclesLeft >= 0) {
                    character.removeComponent(EnemyGracePeriodComponent.class);
                } else {
                    character.saveComponent(enemyGracePeriodComponent);
                }
            }

            if (!character.hasComponent(SettlementRefComponent.class)) {
                spawnEnemyOnCharacter(character);
            }
        }

        enemyQueue.removeIf(enemy -> {
            LocationComponent locComp = enemy.getComponent(LocationComponent.class);
            Vector3f enemyLoc = locComp.getWorldPosition();
            if (settlementEntityManager.checkOutsideAllSettlements(new Vector2i(enemyLoc.getX(), enemyLoc.getZ()))) {
                return false;
            }
            enemy.destroy();
            return true;
        });

        while (enemyQueue.size() > MAX_ENEMIES) {
            enemyQueue.remove().destroy();
        }
    }

    @ReceiveEvent
    public void onDawnEvent(OnDawnEvent event, EntityRef entityRef) {
        while (!enemyQueue.isEmpty()) {
            enemyQueue.remove().destroy();
        }
    }

    @ReceiveEvent
    public void onCharacterRespawn(OnPlayerRespawnedEvent event, EntityRef entity) {
        entity.saveComponent(new EnemyGracePeriodComponent(5));
    }

    private void spawnEnemyOnCharacter(EntityRef character) {
        LocationComponent locationComponent = character.getComponent(LocationComponent.class);

        List<Vector3i> spawnPositions = findSpawnPositions(new Vector3i(locationComponent.getWorldPosition()));
        Optional<Vector3i> potentialPos = spawnPositions.stream().filter(p -> random.nextFloat() > 0.999).findFirst();

        if (potentialPos.isPresent()) {
            spawnOnPosition(potentialPos.get());
        }
    }

    private List<Vector3i> findSpawnPositions(Vector3i centrePos) {
        Vector3i worldPos = new Vector3i(centrePos);
        List<Vector3i> foundPositions = Lists.newArrayList();
        Vector3i blockPos = new Vector3i();
        for (int y = -10; y < 10; y++) {
            for (int z = -30; z < 30; z++) {
                for (int x = -30; x < 30; x++) {
                    blockPos.set(x + worldPos.x, y + worldPos.y, z + worldPos.z);
                    if (isValidSpawnPosition.apply(blockPos)) {
                        foundPositions.add(new Vector3i(blockPos));
                    }
                }
            }
        }
        return foundPositions;
    }

    private boolean isValidSpawnPosition(Vector3i pos) {
        if (!settlementEntityManager.checkOutsideAllSettlements(new Vector2i(pos.x, pos.z))) {
            return false;
        }

        Vector3i below = new Vector3i(pos.x, pos.y - 1, pos.z);
        Block blockBelow = worldProvider.getBlock(below);
        if (!blockBelow.equals(sand)) {
            return false;
        }

        Block blockAtPosition = worldProvider.getBlock(pos);
        if (!blockAtPosition.isPenetrable()) {
            return false;
        }

        Vector3i above = new Vector3i(pos.x, pos.y + 1, pos.z);
        Block blockAbove = worldProvider.getBlock(above);
        if (!blockAbove.equals(air)) {
            return false;
        }

        return true;
    }

    private void spawnOnPosition(Vector3i pos) {
        EntityBuilder entityBuilder = entityManager.newBuilder("MawGooey:mawGooey");
        LocationComponent locationComponent = entityBuilder.getComponent(LocationComponent.class);

        locationComponent.setWorldPosition(pos.toVector3f());
        entityBuilder.saveComponent(locationComponent);
        entityBuilder.addComponent(new NightEnemyComponent());

        enemyQueue.add(entityBuilder.build());
    }

}
