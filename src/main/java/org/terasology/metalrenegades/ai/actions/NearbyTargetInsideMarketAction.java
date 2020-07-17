/*
 * Copyright 2017 MovingBlocks
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
package org.terasology.metalrenegades.ai.actions;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.dynamicCities.buildings.components.DynParcelRefComponent;
import org.terasology.dynamicCities.parcels.DynParcel;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.behavior.BehaviorAction;
import org.terasology.logic.behavior.core.Actor;
import org.terasology.logic.behavior.core.BaseAction;
import org.terasology.logic.behavior.core.BehaviorState;
import org.terasology.math.geom.Rect2i;
import org.terasology.minion.move.MinionMoveComponent;

import java.util.List;
import java.util.Random;
import org.terasology.metalrenegades.ai.component.HomeComponent;
import org.terasology.navgraph.WalkableBlock;

/**
 * Sets a marketkeeper character's {@link MinionMoveComponent} target to a random nearby block inside the area of
 * their market building.
 */
@BehaviorAction(name = "set_target_inside_market")
public class NearbyTargetInsideMarketAction extends BaseAction {

    private static final Logger logger = LoggerFactory.getLogger(NearbyTargetInsideMarketAction.class);

    private static final int RANDOM_BLOCK_ITERATIONS = 10;
    private Random random = new Random();

    private Rect2i marketRegion;

    private int moveProbability = 100;

    @Override
    public void construct(Actor actor) {
        HomeComponent homeComp = actor.getComponent(HomeComponent.class);
        EntityRef buildingEntity = homeComp.building;

        DynParcelRefComponent dynParcelRefComponent = buildingEntity.getComponent(DynParcelRefComponent.class);
        DynParcel dynParcel = dynParcelRefComponent.dynParcel;

        marketRegion = dynParcel.getShape();
    }

    @Override
    public BehaviorState modify(Actor actor, BehaviorState result) {
        if (random.nextInt(100) > (99 - moveProbability)) {
            MinionMoveComponent moveComponent = actor.getComponent(MinionMoveComponent.class);
            if (moveComponent.currentBlock != null) {
                WalkableBlock target = findRandomNearbyBlockInMarket(moveComponent.currentBlock);
                moveComponent.target = target.getBlockPosition().toVector3f();
                actor.save(moveComponent);
            } else {
                return BehaviorState.FAILURE;
            }
        }
        return BehaviorState.SUCCESS;
    }

    /**
     * Finds a block close to the character, with the condition that this block must be inside the market parcel.
     *
     * @param startBlock The block that this characer is currently standing on.
     * @return A random nearby block nearby inside the market area.
     */
    private WalkableBlock findRandomNearbyBlockInMarket(WalkableBlock startBlock) {
        WalkableBlock currentBlock = startBlock;
        for (int i = 0; i < random.nextInt(10) + 3; i++) {
            WalkableBlock[] neighbors = currentBlock.neighbors;
            List<WalkableBlock> existingNeighbors = Lists.newArrayList();

            for (WalkableBlock neighbor : neighbors) {
                if (neighbor != null && marketRegion.contains(neighbor.x(), neighbor.z())) {
                    existingNeighbors.add(neighbor);
                }
            }
            if (existingNeighbors.size() > 0) {
                currentBlock = existingNeighbors.get(random.nextInt(existingNeighbors.size()));
            }
        }
        logger.debug(String.format("Looking for a block: my block is %s, found destination %s", startBlock.getBlockPosition(), currentBlock.getBlockPosition()));
        return currentBlock;
    }

}
