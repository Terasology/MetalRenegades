// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.metalrenegades.world.dynamic;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.joml.Vector3i;
import org.terasology.biomesAPI.Biome;
import org.terasology.core.world.CoreBiome;
import org.terasology.core.world.generator.facetProviders.PositionFilters;
import org.terasology.core.world.generator.facetProviders.SurfaceObjectProvider;
import org.terasology.core.world.generator.facets.BiomeFacet;
import org.terasology.core.world.generator.facets.FloraFacet;
import org.terasology.core.world.generator.rasterizers.FloraType;
import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.utilities.procedural.Noise;
import org.terasology.engine.utilities.procedural.WhiteNoise;
import org.terasology.engine.world.generation.ConfigurableFacetProvider;
import org.terasology.engine.world.generation.Facet;
import org.terasology.engine.world.generation.FacetBorder;
import org.terasology.engine.world.generation.GeneratingRegion;
import org.terasology.engine.world.generation.Produces;
import org.terasology.engine.world.generation.Requires;
import org.terasology.engine.world.generation.facets.SeaLevelFacet;
import org.terasology.engine.world.generation.facets.SurfacesFacet;
import org.terasology.nui.properties.Range;

import java.util.List;
import java.util.Map;

/**
 * For the most part, this is just a copy of DefaultFloraProvider from CoreWorlds, extended with the new biomes.
 */
@Produces(FloraFacet.class)
@Requires({
        @Facet(SeaLevelFacet.class),
        @Facet(value = SurfacesFacet.class, border = @FacetBorder(bottom = 1)),
        @Facet(BiomeFacet.class)
})
public class FloraProvider extends SurfaceObjectProvider<Biome, FloraType> implements ConfigurableFacetProvider {
    private Noise densityNoiseGen;

    private Configuration configuration = new Configuration();

    private Map<FloraType, Float> typeProbs = ImmutableMap.of(
            FloraType.GRASS, 0.85f,
            FloraType.FLOWER, 0.1f,
            FloraType.MUSHROOM, 0.05f);

    private Map<Biome, Float> biomeProbs = ImmutableMap.<Biome, Float>builder()
            .put(MRBiome.RIVER, 0.2f)
            .put(MRBiome.STEPPE, 0.1f)
            .put(MRBiome.ROCKY, 0.001f)
            .put(MRBiome.SCRUBLAND, 0.05f)
            .put(CoreBiome.DESERT, 0.001f).build();

    public FloraProvider() {
        Biome[] biomes = {MRBiome.RIVER, MRBiome.STEPPE, MRBiome.SCRUBLAND, MRBiome.ROCKY, CoreBiome.DESERT};

        for (Biome biome : biomes) {
            float biomeProb = biomeProbs.get(biome);
            for (FloraType type : typeProbs.keySet()) {
                float typeProb = typeProbs.get(type);
                float prob = biomeProb * typeProb;
                register(biome, type, prob);
            }
        }

        register(CoreBiome.DESERT, FloraType.MUSHROOM, 0);
        register(MRBiome.SCRUBLAND, FloraType.GRASS, 0.2f);
    }

    @Override
    public void setSeed(long seed) {
        super.setSeed(seed);

        densityNoiseGen = new WhiteNoise(seed);
    }

    @Override
    public void process(GeneratingRegion region) {
        SurfacesFacet surfaces = region.getRegionFacet(SurfacesFacet.class);
        BiomeFacet biomeFacet = region.getRegionFacet(BiomeFacet.class);

        FloraFacet facet = new FloraFacet(region.getRegion(), region.getBorderForFacet(FloraFacet.class));

        List<Predicate<Vector3i>> filters = getFilters(region);
        populateFacet(facet, surfaces, biomeFacet, filters);

        region.setRegionFacet(FloraFacet.class, facet);
    }

    protected List<Predicate<Vector3i>> getFilters(GeneratingRegion region) {
        List<Predicate<Vector3i>> filters = Lists.newArrayList();

        SeaLevelFacet seaLevel = region.getRegionFacet(SeaLevelFacet.class);
        filters.add(PositionFilters.minHeight(seaLevel.getSeaLevel()));

        filters.add(PositionFilters.probability(densityNoiseGen, configuration.density));

        return filters;
    }

    @Override
    public String getConfigurationName() {
        return "Flora";
    }

    @Override
    public Component getConfiguration() {
        return configuration;
    }

    @Override
    public void setConfiguration(Component configuration) {
        this.configuration = (Configuration) configuration;
    }

    public static class Configuration implements Component<Configuration> {
        @Range(min = 0, max = 1.0f, increment = 0.05f, precision = 2, description = "Define the overall flora density")
        public float density = 0.4f;
    }
}
