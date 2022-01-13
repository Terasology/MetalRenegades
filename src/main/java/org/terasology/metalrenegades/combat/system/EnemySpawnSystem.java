// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.metalrenegades.combat.system;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.module.behaviors.systems.NightTrackerSystem;
import org.terasology.dynamicCities.settlements.SettlementEntityManager;
import org.terasology.engine.entitySystem.entity.EntityBuilder;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.registry.In;
import org.terasology.engine.utilities.random.FastRandom;
import org.terasology.engine.utilities.random.Random;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.chunks.Chunks;
import org.terasology.engine.world.chunks.event.BeforeChunkUnload;
import org.terasology.engine.world.chunks.event.OnChunkLoaded;
import org.terasology.engine.world.sun.OnDawnEvent;
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;
import org.terasology.metalrenegades.combat.component.NightEnemyComponent;
import org.terasology.metalrenegades.minimap.events.AddCharacterToOverlayEvent;
import org.terasology.metalrenegades.minimap.events.RemoveCharacterFromOverlayEvent;

import java.util.List;
import java.util.Queue;

/**
 * Spawns enemies outside the area of settlements at nighttime. These enemies are destroyed if they go inside a city,
 * or the sun rises.
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class EnemySpawnSystem extends BaseComponentSystem implements UpdateSubscriberSystem {

    private static final Logger logger = LoggerFactory.getLogger(EnemySpawnSystem.class);

    /**
     * The time period between processing updates in seconds.
     */
    private static final float UPDATE_PERIOD = 1;

    /**
     * The maximum number of enemies that can spawn in the world.
     */
    private static final int MAX_ENEMIES = 15;

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
     * The amount of time elapsed since the last processed update (spawned/destroyed enemies).
     */
    private float elapsedTime;

    /**
     * Contains all enemies currently in the world. Used to remove enemies in spawn order when the maximum number of
     * enemies is reached.
     */
    private Queue<EntityRef> enemyQueue;

    /**
     * A list of positions of loaded chunks that enemies can be spawned in
     */
    private List<Vector3i> chunkPositions;

    /**
     * True if this system is initialised, false otherwise.
     */
    private boolean ready;

    /**
     * A random value generator used to determine enemy spawn positions.
     */
    private Random random;

    @Override
    public void postBegin() {
        nightTrackerSystem = CoreRegistry.get(NightTrackerSystem.class);
        random = new FastRandom();

        enemyQueue = Queues.newLinkedBlockingQueue();
        chunkPositions = Lists.newArrayList();

        ready = true;
    }

    @Override
    public void update(float delta) {
        // Don't spawn enemies during the day or before system is ready
        if (!nightTrackerSystem.isNight() || !ready) {
            return;
        }

        // TODO: Consider using `DelayManager`'s periodic actions
        elapsedTime += delta;
        if (elapsedTime < UPDATE_PERIOD) {
            return;
        }
        elapsedTime -= UPDATE_PERIOD;

        if (enemyQueue.size() < MAX_ENEMIES) {
            spawnEnemyInWorld();
        }

        // Removes enemies that have entered a settlement
        enemyQueue.removeIf(enemy -> {
            // prevents a rare NPE where an enemy has no location component
            if (!enemy.hasComponent(LocationComponent.class)) {
                removeEnemy(enemy);
                logger.warn("Removed enemy without a location component");
                return true;
            }

            LocationComponent locComp = enemy.getComponent(LocationComponent.class);
            Vector3f enemyLoc = locComp.getWorldPosition(new Vector3f());

            if (enemy.isActive()) {
                return false;
            }
            removeEnemy(enemy);
            logger.debug("Removed inactive enemy at ({}, {}, {}).", enemyLoc.x(), enemyLoc.y(), enemyLoc.z());
            return true;
        });
    }

    @ReceiveEvent
    public void onDawnEvent(OnDawnEvent event, EntityRef entityRef) {
        logger.debug("Dawn event invoked, despawning all enemies.");
        while (!enemyQueue.isEmpty()) {
            removeEnemy(enemyQueue.remove());
        }
    }

    @ReceiveEvent
    public void chunkLoadedEvent(OnChunkLoaded event, EntityRef entity) {
        chunkPositions.add(new Vector3i(event.getChunkPos()));
    }

    @ReceiveEvent
    public void beforeChunkUnload(BeforeChunkUnload event, EntityRef entity) {
        chunkPositions.remove(event.getChunkPos());
    }

    /**
     * Attempts to spawn an enemy in the game world outside of cities in loaded chunks.
     */
    private void spawnEnemyInWorld() {
        Vector3i spawnPosition = findSpawnPosition();

        if (spawnPosition != null) {
            spawnOnPosition(spawnPosition);
        }
    }

    /**
     * Finds a random coordinate position that can be used to spawn enemies.
     *
     * @return A randomly chosen possible spawn position, or null if none could be found.
     */
    private Vector3i findSpawnPosition() {
        if (chunkPositions.isEmpty()) {
            logger.debug("No currently spawned chunks, skipping spawn cycle....");
            return null;
        }

        Vector3i chunkPosition = chunkPositions.get(random.nextInt(chunkPositions.size()));
        Vector3i chunkWorldPosition = chunkPosition.mul(Chunks.SIZE_X, Chunks.SIZE_Y, Chunks.SIZE_Z);
        Vector2i randomColumn = new Vector2i(chunkWorldPosition.x + random.nextInt(Chunks.SIZE_X),
                chunkWorldPosition.z + random.nextInt(Chunks.SIZE_Z));

        if (!worldProvider.isBlockRelevant(chunkWorldPosition)) {
            // 2nd line of defense in case chunk load/unload events are skipped.
            chunkPositions.remove(chunkPosition);
            logger.warn("Inactive chunk requested! Removing chunk from spawn list and skipping spawn cycle");
            return null;
        }

        for (int y = chunkWorldPosition.y - Chunks.SIZE_Y; y < chunkWorldPosition.y + Chunks.SIZE_Y; y++) {
            Vector3i possiblePosition = new Vector3i(randomColumn.x, y, randomColumn.y);
            if (isValidSpawnPosition(possiblePosition)) {
                return possiblePosition;
            }
        }

        logger.debug("No valid position found in column ({}, {}) inside chunk ({}, {}, {}), skipping spawn cycle.",
                randomColumn.x, randomColumn.y, chunkPosition.x, chunkPosition.y, chunkPosition.z);
        return null;
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
        if (blockBelow.isPenetrable()) {
            return false;
        }

        Block blockAtPosition = worldProvider.getBlock(pos);
        if (!blockAtPosition.isPenetrable()) {
            return false;
        }

        Vector3i above = new Vector3i(pos.x, pos.y + 1, pos.z);
        Block blockAbove = worldProvider.getBlock(above);
        if (!blockAbove.isPenetrable()) {
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
        EntityBuilder entityBuilder = entityManager.newBuilder("MetalRenegades:enemyGooey");
        LocationComponent locationComponent = entityBuilder.getComponent(LocationComponent.class);

        locationComponent.setWorldPosition(new Vector3f(pos));
        entityBuilder.saveComponent(locationComponent);
        entityBuilder.addComponent(new NightEnemyComponent());

        EntityRef enemyEntity = entityBuilder.build();
        enemyEntity.send(new AddCharacterToOverlayEvent());
        enemyQueue.add(enemyEntity);
    }

    /**
     * Removes an enemy from the minimap overlay, then destroys the character entity.
     *
     * @param enemy The enemy to remove.
     */
    private void removeEnemy(EntityRef enemy) {
        enemy.send(new RemoveCharacterFromOverlayEvent());
        enemy.destroy();
    }

}
