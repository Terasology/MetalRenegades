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
package org.terasology.metalrenegades.ai;

/**
 * A single need for a citizen character.
 */
public class CitizenNeed {

    /**
     * A type of need the citizen can possess.
     */
    public enum Type {
            FOOD, WATER, SOCIAL, REST
    }

    /**
     * The maximum value of this need.
     */
    private float capacity;

    /**
     * The rate at which the need value reduces, per need cycle.
     */
    private float reductionRate;

    /**
     * The value that the citizen must keep this need above.
     */
    private float goal;

    /**
     * The current need value.
     */
    private float value;

    /**
     * Creates a CitizenNeed with provided settings.
     *
     * @param capacity Preset value for {@link CitizenNeed#capacity}.
     * @param reductionRate Preset value for {@link CitizenNeed#reductionRate}.
     * @param goal Preset value for {@link CitizenNeed#goal}.
     * @param value Preset value for {@link CitizenNeed#value}.
     */
    public CitizenNeed(float capacity, float reductionRate, float goal, float value) {
        this.capacity = capacity;
        this.reductionRate = reductionRate;
        this.goal = goal;
        this.value = value;
    }

    /**
     * Runs an iteration of the need cycle, subtracting {@link CitizenNeed#reductionRate} from {@link CitizenNeed#value}.
     */
    public void runNeedCycle() {
        this.value -= this.reductionRate;
    }

    /**
     * Checks if the current need value is below the goal value. Used to confirm if action is required for this action.
     *
     * @return True if need value is below goal, false otherwise.
     */
    public boolean isBelowGoal() {
        return this.value < this.goal;
    }

    /**
     * Restores the need value to maximum capacity.
     */
    public void restoreNeed() {
        this.value = this.capacity;
    }

}
