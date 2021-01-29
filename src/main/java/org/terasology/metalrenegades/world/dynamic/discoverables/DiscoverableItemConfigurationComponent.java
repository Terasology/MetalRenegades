// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.metalrenegades.world.dynamic.discoverables;

import org.terasology.entitySystem.Component;

import java.util.List;

public class DiscoverableItemConfigurationComponent implements Component {

    /**
     * A list of all item definitions that can be generated in hidden chests.
     */
    public List<List<String>> items;

}
