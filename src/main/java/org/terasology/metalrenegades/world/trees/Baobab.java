// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.metalrenegades.world.trees;

import com.google.common.collect.Maps;
import org.joml.Vector3f;
import org.terasology.engine.world.generator.plugin.RegisterPlugin;
import org.terasology.gf.tree.PartOfTreeComponent;
import org.terasology.gf.tree.lsystem.AdvancedLSystemTreeDefinition;
import org.terasology.gf.tree.lsystem.AxionElementGeneration;
import org.terasology.gf.tree.lsystem.AxionElementReplacement;
import org.terasology.gf.tree.lsystem.DefaultAxionElementGeneration;
import org.terasology.gf.tree.lsystem.LSystemBasedTreeGrowthDefinition;
import org.terasology.gf.tree.lsystem.SimpleAxionElementReplacement;
import org.terasology.gf.tree.lsystem.SurroundAxionElementGeneration;
import org.terasology.gf.tree.lsystem.TreeBlockDefinition;

import java.util.Map;

@RegisterPlugin
public class Baobab extends LSystemBasedTreeGrowthDefinition {
    public static final String ID = "PlantPack:baobab";
    public static final String GENERATED_BLOCK = "MetalRenegades:BaobabSaplingGenerated";
    private final AdvancedLSystemTreeDefinition treeDefinition;

    public Baobab() {
        Map<Character, AxionElementReplacement> replacementMap = Maps.newHashMap();
        Map<Character, AxionElementGeneration> blockMap = Maps.newHashMap();

        TreeBlockDefinition sapling = new TreeBlockDefinition(GENERATED_BLOCK, PartOfTreeComponent.Part.SAPLING);
        TreeBlockDefinition saplingGenerated = new TreeBlockDefinition(GENERATED_BLOCK, PartOfTreeComponent.Part.SAPLING);
        TreeBlockDefinition leaf = new TreeBlockDefinition("PlantPack:BaobabLeaf", PartOfTreeComponent.Part.LEAF);
        TreeBlockDefinition trunk = new TreeBlockDefinition("PlantPack:BaobabTrunk", PartOfTreeComponent.Part.TRUNK);

        replacementMap.put('g', new SimpleAxionElementReplacement("s"));
        replacementMap.put('s', new SimpleAxionElementReplacement("WT"));
        replacementMap.put('T', new SimpleAxionElementReplacement("WWWU")
                .addReplacement(0.6f, "WT"));
        replacementMap.put('U', new SimpleAxionElementReplacement("+B+W+B+")
                .addReplacement(0.8f, "+B+WU"));
        replacementMap.put('B', new SimpleAxionElementReplacement("[&(180)t&(180)&wwwwwwwL]")
                .addReplacement(0.5f, "[&(180)t&(180)&&wwwwwwwL]"));
        replacementMap.put('L', new SimpleAxionElementReplacement("L"));
        replacementMap.put('W', new SimpleAxionElementReplacement("W"));
        replacementMap.put('w', new SimpleAxionElementReplacement("w"));
        replacementMap.put('t', new SimpleAxionElementReplacement("t"));

        blockMap.put('s', new DefaultAxionElementGeneration(sapling, 0));
        blockMap.put('g', new DefaultAxionElementGeneration(saplingGenerated, 0));
        blockMap.put('t', new DefaultAxionElementGeneration(trunk, 1));
        blockMap.put('W', new SurroundAxionElementGeneration(trunk, trunk, 1, 2.1f));
        blockMap.put('w', horizontalGroup(1, trunk));
        blockMap.put('L', horizontalGroup(3, leaf));

        treeDefinition = new AdvancedLSystemTreeDefinition(ID, "g", replacementMap, blockMap, (float) Math.toRadians(35));
    }

    /**
     * Creates a mostly horizontal blob of {@code block}, up to {@code rangeMax} blocks radius at the center y-level.
     * Based on {@link SurroundAxionElementGeneration}.
     */
    private AxionElementGeneration horizontalGroup(int rangeMax, TreeBlockDefinition block) {
        return (callback, position, rotation, axionParameter) -> {
            Vector3f workVector = new Vector3f();

            callback.setMainBlock(position, block);
            int range2 = rangeMax * rangeMax;
            for (int y = -2; y <= 2; y++) {
                int range = rangeMax - Math.abs(y);
                if (range <= 0) {
                    continue;
                }
                for (int x = -range; x <= range; x++) {
                    for (int z = -range; z <= range; z++) {
                        double distanceSquare = x * x + y * y + z * z;
                        if (distanceSquare <= range2) {
                            workVector.set(x, y, z);
                            workVector.add(position);
                            callback.setAdditionalBlock(workVector, block);
                        }
                    }
                }
            }

            callback.advance(1);
        };
    }

    @Override
    protected AdvancedLSystemTreeDefinition getTreeDefinition() {
        return treeDefinition;
    }

    @Override
    public String getPlantId() {
        return ID;
    }

    @Override
    protected String getGeneratedBlock() {
        return GENERATED_BLOCK;
    }
}
