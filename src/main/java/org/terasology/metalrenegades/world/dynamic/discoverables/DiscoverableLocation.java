// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.metalrenegades.world.dynamic.discoverables;

/**
 * A discoverable location placed in {@link DiscoverablesFacet} by {@link DiscoverablesProvider}
 */
public class DiscoverableLocation {

    public Type locationType;

    public DiscoverableLocation(Type locationType) {
        this.locationType = locationType;
    }

    public enum Type {
        WELL, HOUSE
    }

}
