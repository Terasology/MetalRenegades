// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.metalrenegades.world.dynamic;

import org.terasology.biomesAPI.BiomeRegistry;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.registry.In;

import java.util.stream.Stream;

/**
 * Registers the biomes added by Metal Renegades.
 */
@RegisterSystem
public class MRBiomes extends BaseComponentSystem {
    @In
    private BiomeRegistry biomeRegistry;

    /**
     * Registration of systems must be done in preBegin to be early enough.
     */
    @Override
    public void preBegin() {
        Stream.of(MRBiome.values()).forEach(biomeRegistry::registerBiome);
    }
}
