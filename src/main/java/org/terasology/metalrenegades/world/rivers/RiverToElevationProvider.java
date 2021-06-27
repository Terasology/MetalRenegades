// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.metalrenegades.world.rivers;

import org.joml.Vector2f;
import org.joml.Vector2ic;
import org.terasology.biomesAPI.Biome;
import org.terasology.core.world.generator.facets.BiomeFacet;
import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.utilities.procedural.BrownianNoise;
import org.terasology.engine.utilities.procedural.SimplexNoise;
import org.terasology.engine.utilities.procedural.SubSampledNoise;
import org.terasology.engine.utilities.procedural.WhiteNoise;
import org.terasology.engine.world.generation.ConfigurableFacetProvider;
import org.terasology.engine.world.generation.Facet;
import org.terasology.engine.world.generation.GeneratingRegion;
import org.terasology.engine.world.generation.Requires;
import org.terasology.engine.world.generation.Updates;
import org.terasology.engine.world.generation.facets.ElevationFacet;
import org.terasology.engine.world.generation.facets.SeaLevelFacet;
import org.terasology.engine.world.generation.facets.SurfaceHumidityFacet;
import org.terasology.math.TeraMath;
import org.terasology.metalrenegades.world.dynamic.MRBiome;

import java.util.Iterator;


@Requires({@Facet(RiverFacet.class), @Facet(SeaLevelFacet.class)})
@Updates({@Facet(ElevationFacet.class), @Facet(SurfaceHumidityFacet.class), @Facet(BiomeFacet.class)})
public class RiverToElevationProvider implements ConfigurableFacetProvider {
    private static final int SAMPLE_RATE = 4;

    private Configuration configuration = new Configuration();
    private SubSampledNoise steepnessNoise;
    private WhiteNoise whiteNoiseHeight;
    private WhiteNoise whiteNoiseRiver;

    @Override
    public void setSeed(long seed) {
        steepnessNoise = new SubSampledNoise(
                new BrownianNoise(new SimplexNoise(seed + 7), 3),
                new Vector2f(0.0008f, 0.0008f), SAMPLE_RATE);
        whiteNoiseHeight = new WhiteNoise((int) (seed % Integer.MAX_VALUE));
        whiteNoiseRiver = new WhiteNoise((int) (seed % Integer.MAX_VALUE) - 4);
    }

    @Override
    public void process(GeneratingRegion region) {
        RiverFacet rivers = region.getRegionFacet(RiverFacet.class);
        ElevationFacet elevation = region.getRegionFacet(ElevationFacet.class);
        SurfaceHumidityFacet humidity = region.getRegionFacet(SurfaceHumidityFacet.class);
        BiomeFacet biomes = region.getRegionFacet(BiomeFacet.class);
        int seaLevel = region.getRegionFacet(SeaLevelFacet.class).getSeaLevel();

        float[] surfaceHeights = elevation.getInternal();
        float[] riversData = rivers.getInternal();
        float[] humidityData = humidity.getInternal();
        float[] steepnessData = steepnessNoise.noise(elevation.getWorldArea());
        Biome[] biomeData = biomes.getInternal();
        Iterator<Vector2ic> positions = elevation.getWorldArea().iterator();
        for (int i = 0; i < surfaceHeights.length; ++i) {
            float steepness = steepnessData[i];
            float riverFac = TeraMath.clamp(riversData[i]);
            float riverBedElevation =
                    seaLevel - rivers.maxDepth * (riversData[i] * (4 + 4 * steepness) - 3 - 4 * steepness);
            // Never raise the surface to the river bed, erosion only goes downward
            if (riverBedElevation < surfaceHeights[i]) {
                riverFac = (riverFac - 0.7f * steepness) / (1 - 0.9f * steepness);
                riverFac = TeraMath.clamp(riverFac);
                riverFac = TeraMath.fadePerlin(riverFac);
                surfaceHeights[i] = TeraMath.lerp(surfaceHeights[i], riverBedElevation, riverFac);
            }
            humidityData[i] += Math.max(0, 0.2 * (seaLevel - surfaceHeights[i] + 10) * riversData[i]);

            Vector2ic pos = positions.next();
            if (surfaceHeights[i] < seaLevel + 8f + whiteNoiseHeight.noise(pos.x(), pos.y()) * 2
                    && riversData[i] > 0.7 + whiteNoiseRiver.noise(pos.x(), pos.y()) * 0.04) {
                biomeData[i] = MRBiome.RIVER;
            }
        }
    }

    @Override
    public String getConfigurationName() {
        return "River Elevation";
    }

    @Override
    public Component getConfiguration() {
        return configuration;
    }

    @Override
    public void setConfiguration(Component configuration) {
        this.configuration = (Configuration) configuration;
    }

    private static class Configuration implements Component {
    }
}
