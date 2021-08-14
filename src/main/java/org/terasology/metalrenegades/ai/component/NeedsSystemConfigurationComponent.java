// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.metalrenegades.ai.component;

import com.google.common.collect.Lists;
import org.terasology.gestalt.entitysystem.component.Component;

import java.util.List;

public class NeedsSystemConfigurationComponent implements Component<NeedsSystemConfigurationComponent> {

    public int priority;

    public List<List<String>> needsConfigs = Lists.newArrayList();

    @Override
    public void copyFrom(NeedsSystemConfigurationComponent other) {
        this.priority = other.priority;
        this.needsConfigs = Lists.newArrayList(other.needsConfigs);
    }
}
