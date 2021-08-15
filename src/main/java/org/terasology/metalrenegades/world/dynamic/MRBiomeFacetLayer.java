// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.metalrenegades.world.dynamic;

import com.google.common.collect.Maps;
import org.terasology.biomesAPI.Biome;
import org.terasology.core.world.CoreBiome;
import org.terasology.core.world.generator.facets.BiomeFacet;
import org.terasology.engine.world.viewer.layers.NominalFacetLayer;
import org.terasology.engine.world.viewer.layers.Renders;
import org.terasology.engine.world.viewer.layers.ZOrder;
import org.terasology.nui.Color;

import java.util.Map;
import java.util.function.Function;

/**
 * Maps {@link MRBiome} facet to corresponding colors.
 */
@Renders(value = BiomeFacet.class, order = ZOrder.BIOME)
public class MRBiomeFacetLayer extends NominalFacetLayer<Biome> {
    public MRBiomeFacetLayer() {
        super(BiomeFacet.class, new MRBiomeColors());
    }

    static class MRBiomeColors implements Function<Biome, Color> {
        private final Map<Biome, Color> biomeColors = Maps.newHashMap();

        MRBiomeColors() {
            biomeColors.put(CoreBiome.DESERT, new Color(0xb0a087ff));
            biomeColors.put(CoreBiome.MOUNTAINS, new Color(0x899a47ff));
            biomeColors.put(CoreBiome.PLAINS, new Color(0x80b068ff));
            biomeColors.put(CoreBiome.SNOW, new Color(0x99ffffff));
            biomeColors.put(CoreBiome.FOREST, new Color(0x439765ff));
            biomeColors.put(CoreBiome.OCEAN, new Color(0x44447aff));
            biomeColors.put(CoreBiome.BEACH, new Color(0xd0c087ff));
            biomeColors.put(MRBiome.RIVER, new Color(0x3dc4e2ff));
            biomeColors.put(MRBiome.ROCKY, new Color(0x757166ff));
            biomeColors.put(MRBiome.STEPPE, new Color(0x569338ff));
            biomeColors.put(MRBiome.SCRUBLAND, new Color(0x68552cff));
        }

        @Override
        public Color apply(Biome biome) {
            return biomeColors.get(biome);
        }
    }
}
