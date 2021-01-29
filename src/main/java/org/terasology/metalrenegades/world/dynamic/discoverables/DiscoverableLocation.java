// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.metalrenegades.world.dynamic.discoverables;

/**
 * A discoverable location placed in {@link DiscoverablesFacet} by {@link DiscoverablesProvider}
 */
public class DiscoverableLocation {

    public enum Type {
            WELL, HOUSE
    }

    public Type locationType;

    public DiscoverableLocation(Type locationType) {
        this.locationType = locationType;
    }

}
