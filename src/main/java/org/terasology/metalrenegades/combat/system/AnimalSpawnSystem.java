// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.metalrenegades.combat;

import org.terasology.dynamicCities.settlements.SettlementEntityManager;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.registry.In;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.math.geom.Vector2i;
import org.terasology.math.geom.Vector3i;
import org.terasology.wildAnimals.AnimalSpawnConfig;
import org.terasology.wildAnimals.system.WildAnimalsSpawnSystem;

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
        configuration.SPAWN_CHANCE_IN_PERCENT = 1;
        configuration.MAX_DEER_GROUP_SIZE = 5;
        configuration.MIN_DEER_GROUP_SIZE = 2;
        configuration.MIN_GROUND_PER_DEER = 10;

        spawnSystem.setConfig(configuration);

        sand = blockManager.getBlock("CoreAssets:Sand");
        air = blockManager.getBlock(BlockManager.AIR_ID);
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
        return blockAbove.equals(air);
    }
}
