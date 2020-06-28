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
package org.terasology.metalrenegades.ai.system;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.nameTags.NameTagComponent;
import org.terasology.math.geom.Vector3f;
import org.terasology.metalrenegades.ai.event.CitizenSpawnedEvent;
import org.terasology.namegenerator.creature.CreatureAssetTheme;
import org.terasology.namegenerator.creature.CreatureNameProvider;
import org.terasology.rendering.logic.SkeletalMeshComponent;
import org.terasology.rendering.nui.Color;
import org.terasology.utilities.random.FastRandom;
import org.terasology.utilities.random.Random;

/**
 * Randomizes particular characteristics for city NPC characters.
 */
@RegisterSystem(value = RegisterMode.AUTHORITY)
public class CitizenPersonalitySystem extends BaseComponentSystem {

    /**
     * The +- range of possible model scaling differences.
     */
    public static final float SCALE_RANGE = 0.1f;

    /**
     * The ratio of model scale change to height offset change. This is included to prevent characters from hovering
     * or sinking into the ground when grown/shrunk.
     */
    public static final float HEIGHT_OFFSET_RATIO = 8;

    /**
     * A random number generator for all character characteristics.
     */
    private Random random;

    @Override
    public void postBegin() {
        long seed = this.hashCode() & 0x921233;
        random = new FastRandom(seed);
    }

    @ReceiveEvent
    public void onCitizenSpawn(CitizenSpawnedEvent citizenSpawnedEvent, EntityRef target) {
        NameTagComponent nameTagComponent = new NameTagComponent();

        CreatureNameProvider nameProvider = new CreatureNameProvider(random.nextLong(), CreatureAssetTheme.OLD_WEST);
        nameTagComponent.text = nameProvider.generateName();
        nameTagComponent.textColor = Color.GREEN;
        nameTagComponent.yOffset = 0.5f;
        nameTagComponent.scale = 1f;

        target.saveComponent(nameTagComponent);

        SkeletalMeshComponent skeletalMeshComponent = target.getComponent(SkeletalMeshComponent.class);

        float scaleDiff = random.nextFloat(-SCALE_RANGE, SCALE_RANGE);
        float scale = skeletalMeshComponent.scale.x + scaleDiff;

        skeletalMeshComponent.scale = new Vector3f(scale, scale, scale);
        skeletalMeshComponent.heightOffset = skeletalMeshComponent.heightOffset + scaleDiff * HEIGHT_OFFSET_RATIO;
        target.saveComponent(skeletalMeshComponent);
    }

}
