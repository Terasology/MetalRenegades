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
package org.terasology.metalrenegades.combat;

import org.terasology.dynamicCities.settlements.SettlementEntityManager;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.math.geom.Vector2i;
import org.terasology.math.geom.Vector3i;
import org.terasology.registry.In;
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

        WildAnimalsSpawnSystem.Configuration configuration = new WildAnimalsSpawnSystem.Configuration();
        configuration.SPAWN_CHANCE_IN_PERCENT = 2;
        configuration.MAX_DEER_GROUP_SIZE = 5;
        configuration.MIN_DEER_GROUP_SIZE = 2;
        configuration.MIN_GROUND_PER_DEER = 10;

        spawnSystem.setConfig(configuration);

        sand = blockManager.getBlock("core:Sand");
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
        if (!blockAbove.equals(air)) {
            return false;
        }

        return true;
    }
}
