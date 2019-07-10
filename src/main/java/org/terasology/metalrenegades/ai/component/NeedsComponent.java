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
package org.terasology.metalrenegades.ai.component;

import org.terasology.entitySystem.Component;

/**
 * Component which keeps track of a citizens current need status.
 */
public class NeedsComponent implements Component {

    /**
     * The maximum food value that this character can have.
     */
    public float maxFoodCapacity = 20;

    /**
     * The rate of reduction of food value, in food unit per needs check.
     */
    public float foodReductionRate = 1f;

    /**
     * The food minimum, when foodValue slips below this value a particular behavior is initiated.
     */
    public float foodGoal = 5;

    /**
     * The current food value of this citizen.
     */
    public float foodValue = 15;

    /**
     * The maximum thirst value that this character can have.
     */
    public float maxThirstCapacity = 20;

    /**
     * The rate of reduction of thirst value, in thirst unit per needs check.
     */
    public float thirstReductionRate = 2f;

    /**
     * The thirst minimum, when thirstValue slips below this value a particular behavior is initiated.
     */
    public float thirstGoal = 8;

    /**
     * The current thirst value of this citizen.
     */
    public float thirstValue = 20;

    /**
     * The maximum rest value that this character can have.
     */
    public float maxRestCapacity = 50f;

    /**
     * The rate of reduction of rest value, in rest unit per needs check.
     */
    public float restReductionRate = 1f;

    /**
     * The rest minimum, when restValue slips below this value a particular behavior is initiated.
     */
    public float restGoal = 20f;

    /**
     * The current rest value of this citizen.
     */
    public float restValue = 50f;

    /**
     * The maximum social value that this character can have.
     */
    public float maxSocialCapacity = 30f;

    /**
     * The rate of reduction of social value, in social unit per needs check.
     */
    public float socialReductionRate = 2f;

    /**
     * The social minimum, when socialValue slips below this value a particular behavior is initiated.
     */
    public float socialGoal = 15f;

    /**
     * The current social value of this citizen.
     */
    public float socialValue = 30f;
}
