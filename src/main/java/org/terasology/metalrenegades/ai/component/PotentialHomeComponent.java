// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.metalrenegades.ai.component;

import com.google.common.collect.Lists;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;

import java.util.List;

/**
 * A component for buildings with the ability to spawn/provide shelter for citizens.
 */
public class PotentialHomeComponent implements Component {

    /**
     * The list of citizen characters inside this home.
     */
    public List<EntityRef> citizens = Lists.newArrayList();

    /**
     * The maximum number of possible citizen characters in this home.
     */
    public int maxCitizens;

}
