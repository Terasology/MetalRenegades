// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
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
     * The type of this CitizenNeed
     */
    private Type needType;

    /**
     * Creates a CitizenNeed with provided settings.
     *
     * @param capacity Preset value for {@link CitizenNeed#capacity}.
     * @param reductionRate Preset value for {@link CitizenNeed#reductionRate}.
     * @param goal Preset value for {@link CitizenNeed#goal}.
     */
    public CitizenNeed(Type needType, float capacity, float reductionRate, float goal) {
        this.needType = needType;
        this.capacity = capacity;
        this.reductionRate = reductionRate;
        this.goal = goal;
        this.value = capacity;
    }

    /**
     * Returns the type of need that this CitizenNeed is.
     *
     * @return This need type.
     */
    public Type getNeedType() {
        return this.needType;
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

    @Override
    public String toString() {
        return Math.round(this.value) + "/" + Math.round(this.capacity);
    }

}
