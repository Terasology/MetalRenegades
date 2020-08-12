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

/**
 * Spawns enemies around players that travel outside the area of a settlement at nighttime. These enemies are destroyed
 * if the player goes back inside a city, or the sun rises.
 */
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

    /**
     * A block definition for sand, used to detect valid spawn positions.
     */
    private Block sand;

    /**
     * A block definition for an air "block", used to detect valid spawn positions.
     */
    private Block air;

    /**
     * The number of update cycles left until enemies are spawned/destroyed again.
     */
    private int cyclesLeft;

    /**
     * Contains all enemies currently in the world. Used to remove enemies in spawn order when the maximum number of
     * enemies is reached.
     */
    private Queue<EntityRef> enemyQueue;

    /**
     * The maximum number of enemies that can spawn in the world.
     */
    private static final int MAX_ENEMIES = 20;

    /**
     * True if this system is initialised, false otherwise.
     */
    private boolean ready;

    @Override
    public void postBegin() {
        sand = blockManager.getBlock("CoreAssets:Sand");
        air = blockManager.getBlock(BlockManager.AIR_ID);

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

            // Prevents enemy spawning if the player just respawned, to allow them to prepare.
            if (character.hasComponent(EnemyGracePeriodComponent.class)) {
                EnemyGracePeriodComponent enemyGracePeriodComponent =
                        character.getComponent(EnemyGracePeriodComponent.class);
                enemyGracePeriodComponent.cyclesLeft--;

                if (enemyGracePeriodComponent.cyclesLeft >= 0) {
                    character.removeComponent(EnemyGracePeriodComponent.class);
                } else {
                    character.saveComponent(enemyGracePeriodComponent);
                }
                continue;
            }

            if (!character.hasComponent(SettlementRefComponent.class)) {
                spawnEnemyOnCharacter(character);
            }
        }

        // Removes enemies that have entered a settlement
        enemyQueue.removeIf(enemy -> {
            LocationComponent locComp = enemy.getComponent(LocationComponent.class);
            Vector3f enemyLoc = locComp.getWorldPosition();
            if (settlementEntityManager.checkOutsideAllSettlements(new Vector2i(enemyLoc.getX(), enemyLoc.getZ()))) {
                return false;
            }
            enemy.destroy();
            return true;
        });

        // If there are too many enemies in the world, remove the oldest enemies to make room.
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

    /**
     * Spawns an enemy around the area of a particular character entity.
     *
     * @param character The spawn target character.
     */
    private void spawnEnemyOnCharacter(EntityRef character) {
        LocationComponent locationComponent = character.getComponent(LocationComponent.class);

        List<Vector3i> spawnPositions = findSpawnPositions(new Vector3i(locationComponent.getWorldPosition()));
        Optional<Vector3i> potentialPos = spawnPositions.stream().findFirst();

        if (potentialPos.isPresent()) {
            spawnOnPosition(potentialPos.get());
        }
    }

    /**
     * Searches a 60x20x60 area around a given position for acceptable spawn positions.
     *
     * @param centrePos The position to search around.
     * @return A list of possible spawn positions.
     */
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

    /**
     * Returns true if the given position is a valid position to spawn an enemy character.
     *
     * @param pos The position to check.
     * @return If the position is valid or not.
     */
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

    /**
     * Spawns an enemy on the given world coordinate.
     *
     * @param pos The position to spawn a character on top of.
     */
    private void spawnOnPosition(Vector3i pos) {
        EntityBuilder entityBuilder = entityManager.newBuilder("MawGooey:mawGooey");
        LocationComponent locationComponent = entityBuilder.getComponent(LocationComponent.class);

        locationComponent.setWorldPosition(pos.toVector3f());
        entityBuilder.saveComponent(locationComponent);
        entityBuilder.addComponent(new NightEnemyComponent());

        enemyQueue.add(entityBuilder.build());
    }

}
