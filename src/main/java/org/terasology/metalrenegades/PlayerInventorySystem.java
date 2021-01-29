// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.metalrenegades;

import org.joml.Vector3f;
import org.terasology.assets.management.AssetManager;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.characters.events.PlayerDeathEvent;
import org.terasology.logic.console.commandSystem.annotations.Command;
import org.terasology.logic.console.commandSystem.annotations.Sender;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.inventory.ItemCommands;
import org.terasology.logic.inventory.events.DropItemRequest;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.permission.PermissionManager;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.registry.In;
import org.terasology.world.block.BlockManager;

import java.util.HashMap;
import java.util.Random;

/**
 * Handles the inventory of the player at spawn and death
 */
@RegisterSystem
public class PlayerInventorySystem extends BaseComponentSystem {
    /**
     * Parameters for item drop position
     */
    private static final Vector3f OFFSET = new Vector3f(1, 1, 1);
    private static final float BOUND = 2f;

    @In
    private ItemCommands itemCommands;

    @In
    private BlockManager blockManager;

    @In
    private InventoryManager inventoryManager;

    @In
    private EntityManager entityManager;

    @In
    private AssetManager assetManager;

    @In
    private LocalPlayer localPlayer;

    private HashMap<String, Integer> testingItems = new HashMap<>();

    @Override
    public void postBegin() {
        testingItems.put("MetalRenegades:pistol", 1);
        testingItems.put("MetalRenegades:bulletItem", 32);
        testingItems.put("CoreAssets:pickaxe", 1);
        testingItems.put("CoreAssets:shovel", 1);
        testingItems.put("CoreAssets:axe", 1);
        testingItems.put("CoreAdvancedAssets:dynamite", 1);
        testingItems.put("CoreAdvancedAssets:gun", 1);
        testingItems.put("CoreAssets:Torch", 64);
    }

    @ReceiveEvent
    public void onPlayerDeath(PlayerDeathEvent event, EntityRef character) {
        if (character.equals(localPlayer.getCharacterEntity())) {
            LocationComponent locationComponent = character.getComponent(LocationComponent.class);
            Vector3f position = locationComponent.getWorldPosition(new Vector3f());
            position.add(OFFSET);

            Random rnd = new Random();
            int numSlots = inventoryManager.getNumSlots(character);
            for (int i = 0; i < numSlots; i++) {
                EntityRef current = inventoryManager.getItemInSlot(character, i);
                if (!current.equals(EntityRef.NULL)) {
                    // Get a random position near the position calculated above
                    // so that the items don't all drop at the same point
                    Vector3f currentItemPos = new Vector3f(position);
                    currentItemPos.add(rnd.nextFloat() * BOUND,0,rnd.nextFloat() * BOUND);

                    character.send(new DropItemRequest(
                        current,
                        character,
                        new org.joml.Vector3f(),
                        currentItemPos,
                        inventoryManager.getStackSize(current)
                    ));
                }
            }
        }
    }

    @Command(shortDescription = "Gives basic testing items for Metal Renegades", requiredPermission = PermissionManager.CHEAT_PERMISSION)
    public String testingKitMR(@Sender EntityRef sender) {
        testingItems.entrySet().stream().forEach(e -> itemCommands.give(sender, e.getKey(), e.getValue(), null));
        return "Testing item kit has been given.";
    }
}
