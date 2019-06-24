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
 * A component which gives time-detection capabilities to a character's behavior.
 */
public class TimeSensitiveComponent implements Component{

    /**
     * The current world game time, where a change of 1 is equal to one day.
     */
    public float worldTime;

    /**
     * The current world game time, relative to the start of the day.
     */
    public float dayTime;

    /**
     * Current day/night status defined by the time range in {@link org.terasology.metalrenegades.ai.system.TimeSensitiveSystem}.
     */
    public boolean isNight;

}
