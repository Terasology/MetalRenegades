// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.metalrenegades.world.dynamic.roads;

import org.terasology.cities.BlockType;

/**
 * A custom implementation of BlockType meant only for Rails, to register rail blocks in a BlockTheme.
 */
public enum RailBlockType implements BlockType {
    BASE,
    RAIL
}
