// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.metalrenegades.combat.system;

import org.joml.Vector2i;
import org.joml.Vector3i;
import org.terasology.dynamicCities.settlements.SettlementEntityManager;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.registry.In;
import org.terasology.wildAnimals.AnimalSpawnConfig;
import org.terasology.wildAnimals.system.WildAnimalsSpawnSystem;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;

@RegisterSystem(RegisterMode.AUTHORITY)
public class AnimalSpawnSystem extends BaseComponentSystem {
    @In
    private WildAnimalsSpawnSystem spawnSystem;

    @In
    private WorldProvider worldProvider;

    @In
    private BlockManager blockManager;

    @In
    private SettlementEntityManager settlementEntityManager;

    private Block sand;
    private Block air;

    @Override
    public void initialise() {
        spawnSystem.setSpawnCondition(this::isValidSpawnPosition);

        AnimalSpawnConfig configuration = new AnimalSpawnConfig();
        configuration.spawnChanceInPercent = 1;
        configuration.maxFlockSize = 5;
        configuration.minFlockSize = 2;
        configuration.minGroundPerFlockAnimal = 10;

        spawnSystem.setConfig(configuration);

        sand = blockManager.getBlock("CoreAssets:Sand");
        air = blockManager.getBlock(BlockManager.AIR_ID);
    }

    private boolean isValidSpawnPosition(Vector3i pos) {
        if (!settlementEntityManager.checkOutsideAllSettlements(new Vector2i(pos.x(), pos.z()))) {
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
}
