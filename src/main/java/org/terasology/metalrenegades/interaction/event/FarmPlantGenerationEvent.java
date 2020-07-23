/*
 * Copyright 2020 MovingBlocks
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
package org.terasology.metalrenegades.interaction.event;

import org.terasology.entitySystem.event.Event;

/**
 * Fired when a city farm has crops generated on it.
 */
public class FarmPlantGenerationEvent implements Event {

    /**
     * The prefab name of the bush/vine that will be generated on the farm.
     */
    public String plantName;

    /**
     * The number of blocks from the centre of the farm that crops will be generated on.
     * This number does not include the centre block.
     */
    public int plantableRadius;

    /**
     * The chance that any particular square on the farm will have a planted crop.
     */
    public double genChance;

    public FarmPlantGenerationEvent(String plant, int radius, double chance) {
        this.plantName = plant;
        this.plantableRadius = radius;
        this.genChance = chance;
    }

}
