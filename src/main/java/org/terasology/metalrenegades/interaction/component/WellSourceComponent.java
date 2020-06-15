/*
 * Copyright 2018 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.metalrenegades.interaction.component;

import org.terasology.entitySystem.Component;

/**
 * Indicates a water cup source block, to be used inside wells.
 */
public class WellSourceComponent implements Component {

    /**
     * The number of water refills remaining in this well. Replenishes after a certain period of time.
     */
    public int waterRefills;

    /**
     * The maximum number of refills that this well can have.
     */
    public int maxWaterRefills;

}
