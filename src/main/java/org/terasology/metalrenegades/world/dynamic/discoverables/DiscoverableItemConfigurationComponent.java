// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.metalrenegades.world.dynamic.discoverables;

import com.google.common.collect.Lists;
import org.terasology.gestalt.entitysystem.component.Component;

import java.util.List;
import java.util.stream.Collectors;

public class DiscoverableItemConfigurationComponent implements Component<DiscoverableItemConfigurationComponent> {

    /**
     * A list of all item definitions that can be generated in hidden chests.
     */
    public List<List<String>> items = Lists.newArrayList();

    @Override
    public void copyFrom(DiscoverableItemConfigurationComponent other) {
        this.items = other.items.stream().map(Lists::newArrayList).collect(Collectors.toList());
    }
}
