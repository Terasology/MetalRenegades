// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.metalrenegades.ai.system;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.nameTags.NameTagComponent;
import org.terasology.engine.rendering.logic.SkeletalMeshComponent;
import org.terasology.engine.utilities.random.FastRandom;
import org.terasology.engine.utilities.random.Random;
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;
import org.terasology.metalrenegades.ai.event.CitizenSpawnedEvent;
import org.terasology.namegenerator.creature.CreatureAssetTheme;
import org.terasology.namegenerator.creature.CreatureNameProvider;
import org.terasology.nui.Color;

/**
 * Randomizes particular characteristics for city NPC characters.
 */
@RegisterSystem(value = RegisterMode.AUTHORITY)
public class CitizenPersonalitySystem extends BaseComponentSystem {

    /**
     * The +- range of possible model scaling differences.
     */
    public static final float SCALE_RANGE = 0.5f;

    /**
     * The ratio of model scale change to height offset change. This is included to prevent characters from hovering
     * or sinking into the ground when grown/shrunk.
     */
    public static final float HEIGHT_OFFSET_RATIO = 2f;

    /**
     * A random number generator for all character characteristics.
     */
    private Random random;

    private CreatureNameProvider nameProvider;

    @Override
    public void postBegin() {
        long seed = this.hashCode() & 0x921233;
        random = new FastRandom(seed);
        nameProvider = new CreatureNameProvider(random.nextLong(), CreatureAssetTheme.OLD_WEST);
    }

    @ReceiveEvent
    public void onCitizenSpawn(CitizenSpawnedEvent citizenSpawnedEvent, EntityRef target) {
        NameTagComponent nameTagComponent = new NameTagComponent();

        nameTagComponent.text = nameProvider.generateName();
        nameTagComponent.textColor = Color.GREEN;
        nameTagComponent.yOffset = 1.7f;
        nameTagComponent.scale = 1f;

        target.saveComponent(nameTagComponent);

        float scaleDiff = random.nextFloat(-SCALE_RANGE, SCALE_RANGE);
        float scaleFactor = scaleDiff + 1;

        SkeletalMeshComponent skeletalMeshComponent = target.getComponent(SkeletalMeshComponent.class);
        skeletalMeshComponent.scale.mul(scaleFactor);
        skeletalMeshComponent.heightOffset += scaleDiff * HEIGHT_OFFSET_RATIO;
        target.saveComponent(skeletalMeshComponent);
    }

}
