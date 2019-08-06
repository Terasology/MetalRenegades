/*
 * Copyright 2019 MovingBlocks
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
package org.terasology.metalrenegades.quests;

import org.terasology.math.geom.Rect2f;
import org.terasology.minimap.overlays.MinimapOverlay;
import org.terasology.rendering.nui.Canvas;

public class TaskOverlay implements MinimapOverlay {
    @Override
    public void render(Canvas canvas, Rect2f worldRect) {
        // TODO
    }

    @Override
    public int getZOrder() {
        return 0;
    }
}
