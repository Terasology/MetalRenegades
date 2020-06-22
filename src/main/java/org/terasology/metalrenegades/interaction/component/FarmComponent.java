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
package org.terasology.metalrenegades.interaction.component;

import org.terasology.entitySystem.Component;

/**
 * Component attached to farms inside cities that have not yet generated any crops. This component is removed when the
 * farm crops are generated.
 */
public class FarmComponent implements Component {

    /**
     * The number of blocks from the centre of the farm that crops can generate on. This number does not include
     * the centre block.
     */
    public int plantableRadius;

    /**
     * The number of world time cycles until the farm should generate crops.
     */
    public int generationCycles = 4;

    /**
     * Upon generation, the chance that any particular square on the farm will have a planted crop.
     */
    public double genChance = 0.2;

}
