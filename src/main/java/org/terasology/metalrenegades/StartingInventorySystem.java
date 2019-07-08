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
package org.terasology.metalrenegades;

import org.terasology.combatSystem.inventory.CombatStartingInventory;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.registry.In;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.items.BlockItemFactory;

import java.util.HashSet;
import java.util.Set;

@RegisterSystem(RegisterMode.AUTHORITY)
public class StartingInventorySystem extends BaseComponentSystem {
    @In
    private BlockManager blockManager;

    @In
    private InventoryManager inventoryManager;

    @In
    private EntityManager entityManager;

    @In
    private CombatStartingInventory combatStartingInventory;

    @Override
    public void postBegin() {
        int numTorches = 99;
        int numBullets = 32;

        BlockItemFactory blockItemFactory = new BlockItemFactory(entityManager);
        Set<EntityRef> items = new HashSet<>();

        items.add(entityManager.create("MetalRenegades:pistol"));

        for (int i = 0; i < numBullets; i++) {
            items.add(entityManager.create("MetalRenegades:bulletItem"));
        }

        items.add(entityManager.create("core:pickaxe"));
        items.add(entityManager.create("core:shovel"));
        items.add(entityManager.create("core:axe"));
        items.add(blockItemFactory.newInstance(blockManager.getBlockFamily("core:Torch"), numTorches));
        items.add(entityManager.create("core:explodeTool"));
        items.add(entityManager.create("core:railgunTool"));

        combatStartingInventory.setItems(items);
    }
}
