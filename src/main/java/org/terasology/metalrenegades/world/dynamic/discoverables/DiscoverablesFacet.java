// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.metalrenegades.world.dynamic.discoverables;

import org.terasology.math.Region3i;
import org.terasology.world.generation.Border3D;
import org.terasology.world.generation.facets.base.SparseObjectFacet3D;

/**
 * Contains information about discoverable chest locations.
 */
public class DiscoverablesFacet extends SparseObjectFacet3D<DiscoverableLocation> {

    public DiscoverablesFacet(Region3i targetRegion, Border3D border) {
        super(targetRegion, border);
    }

}
