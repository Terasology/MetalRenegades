// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.metalrenegades.ai.component;

import com.google.common.collect.Lists;
import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.network.Replicate;
import org.terasology.metalrenegades.ai.CitizenNeed;

import java.util.List;

/**
 * Component which keeps track of a citizens current need status.
 */
public class NeedsComponent implements Component {

    @Replicate
    public List<CitizenNeed> needs = Lists.newArrayList();

}
