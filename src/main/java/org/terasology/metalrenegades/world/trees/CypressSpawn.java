// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.metalrenegades.world.trees;

import org.terasology.engine.world.generator.plugin.RegisterPlugin;
import org.terasology.gf.PlantType;
import org.terasology.gf.generator.GrowthBasedPlantSpawnDefinition;

@RegisterPlugin
public class CypressSpawn extends GrowthBasedPlantSpawnDefinition {
    public CypressSpawn() {
        super(PlantType.TREE, Cypress.ID, "MetalRenegades:steppe", 0.5f, 0.0007f, x -> true);
    }
}
