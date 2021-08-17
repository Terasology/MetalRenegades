// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.metalrenegades.world.trees;

import com.google.common.collect.Maps;
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
public class Cypress extends LSystemBasedTreeGrowthDefinition {
    public static final String ID = "PlantPack:cypress";
    public static final String GENERATED_BLOCK = "MetalRenegades:CypressSaplingGenerated";
    private final AdvancedLSystemTreeDefinition treeDefinition;

    public Cypress() {
        Map<Character, AxionElementReplacement> replacementMap = Maps.newHashMap();
        Map<Character, AxionElementGeneration> blockMap = Maps.newHashMap();

        TreeBlockDefinition sapling = new TreeBlockDefinition("MetalRenegades:CypressSapling", PartOfTreeComponent.Part.SAPLING);
        TreeBlockDefinition saplingGenerated = new TreeBlockDefinition(GENERATED_BLOCK, PartOfTreeComponent.Part.SAPLING);
        TreeBlockDefinition leaf = new TreeBlockDefinition("PlantPack:CypressLeaf", PartOfTreeComponent.Part.LEAF);
        TreeBlockDefinition trunk = new TreeBlockDefinition("PlantPack:CypressTrunk", PartOfTreeComponent.Part.TRUNK);

        replacementMap.put('g', new SimpleAxionElementReplacement("ATTTttL"));
        replacementMap.put('s', new SimpleAxionElementReplacement("ATTTttL"));
        replacementMap.put('A', new SimpleAxionElementReplacement("")
            .addReplacement(0.7f, "&"));
        replacementMap.put('S', new SimpleAxionElementReplacement("")
            .addReplacement(0.6f, "+S"));
        replacementMap.put('T', new SimpleAxionElementReplacement("t")
            .addReplacement(0.25f, "[S^^TTTttL]&t"));
        replacementMap.put('L', new SimpleAxionElementReplacement("L"));
        replacementMap.put('t', new SimpleAxionElementReplacement("t"));

        blockMap.put('s', new DefaultAxionElementGeneration(sapling, 0));
        blockMap.put('g', new DefaultAxionElementGeneration(saplingGenerated, 0));
        blockMap.put('t', new DefaultAxionElementGeneration(trunk, 1));
        blockMap.put('L', new SurroundAxionElementGeneration(leaf, leaf, 1, 1.5f));

        treeDefinition = new AdvancedLSystemTreeDefinition(ID, "g", replacementMap, blockMap, (float) Math.toRadians(35));
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
