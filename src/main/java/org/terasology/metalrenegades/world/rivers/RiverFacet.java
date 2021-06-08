// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.metalrenegades.world.rivers;

import org.terasology.engine.world.block.BlockRegion;
import org.terasology.engine.world.generation.Border3D;
import org.terasology.engine.world.generation.facets.base.BaseFieldFacet2D;

public class RiverFacet extends BaseFieldFacet2D {
    public float maxDepth;

    public RiverFacet(BlockRegion targetRegion, Border3D border, float maxDepth) {
        super(targetRegion, border);
        this.maxDepth = maxDepth;
    }
}
