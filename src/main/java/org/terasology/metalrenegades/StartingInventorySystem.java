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

import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.inventory.InventoryComponent;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.players.event.OnPlayerSpawnedEvent;
import org.terasology.protobuf.EntityData;
import org.terasology.registry.In;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.items.BlockItemFactory;

@RegisterSystem(RegisterMode.AUTHORITY)
public class StartingInventorySystem extends BaseComponentSystem {
    @In
    private BlockManager blockManager;

    @In
    private InventoryManager inventoryManager;

    @In
    private EntityManager entityManager;

    @ReceiveEvent(components = InventoryComponent.class)
    public void onPlayerSpawned(OnPlayerSpawnedEvent event, EntityRef player) {
        int startingBullets = 32;

        BlockItemFactory blockItemFactory = new BlockItemFactory(entityManager);

        inventoryManager.giveItem(player, EntityRef.NULL, entityManager.create("core:pickaxe"));
        inventoryManager.giveItem(player, EntityRef.NULL, entityManager.create("core:axe"));
        inventoryManager.giveItem(player, EntityRef.NULL, entityManager.create("core:shovel"));
        inventoryManager.giveItem(player, EntityRef.NULL, blockItemFactory.newInstance(blockManager.getBlockFamily("core:Torch"), 99));
        inventoryManager.giveItem(player, EntityRef.NULL, entityManager.create("core:explodeTool"));
        inventoryManager.giveItem(player, EntityRef.NULL, entityManager.create("core:railgunTool"));
        inventoryManager.giveItem(player, EntityRef.NULL, entityManager.create("MetalRenegades:pistol"));

        for (int i = 0; i < startingBullets; i++) {
            inventoryManager.giveItem(player, EntityRef.NULL, entityManager.create("MetalRenegades:bulletItem"));
        }
    }
}
