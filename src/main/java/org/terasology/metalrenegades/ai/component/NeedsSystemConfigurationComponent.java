// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.metalrenegades.ai.component;

import org.terasology.engine.entitySystem.Component;

import java.util.List;

public class NeedsSystemConfigurationComponent implements Component {

    public int priority;

    public List<List<String>> needsConfigs;

}
