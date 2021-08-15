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
import org.terasology.engine.world.generation.ScalableFacetProvider;
import org.terasology.engine.world.generation.Updates;
import org.terasology.engine.world.generation.facets.ElevationFacet;
import org.terasology.engine.world.generation.facets.SeaLevelFacet;
import org.terasology.engine.world.generation.facets.SurfaceHumidityFacet;
import org.terasology.math.TeraMath;
import org.terasology.metalrenegades.world.dynamic.MRBiome;

import java.util.Iterator;


@Requires({@Facet(RiverFacet.class), @Facet(SeaLevelFacet.class), @Facet(SurfaceHumidityFacet.class)})
@Updates({@Facet(ElevationFacet.class), @Facet(BiomeFacet.class)})
public class RiverToElevationProvider implements ConfigurableFacetProvider, ScalableFacetProvider {
    private static final int SAMPLE_RATE = 4;

    private Configuration configuration = new Configuration();
    private SubSampledNoise steepnessNoise;
    private WhiteNoise whiteNoiseRiver;

    @Override
    public void setSeed(long seed) {
        steepnessNoise = new SubSampledNoise(
                new BrownianNoise(new SimplexNoise(seed + 7), 3),
                new Vector2f(0.0008f, 0.0008f), SAMPLE_RATE);
        whiteNoiseRiver = new WhiteNoise((int) (seed % Integer.MAX_VALUE) - 4);
    }

    @Override
    public void process(GeneratingRegion region, float scale) {
        RiverFacet rivers = region.getRegionFacet(RiverFacet.class);
        ElevationFacet elevation = region.getRegionFacet(ElevationFacet.class);
        BiomeFacet biomes = region.getRegionFacet(BiomeFacet.class);
        SurfaceHumidityFacet humidityFacet = region.getRegionFacet(SurfaceHumidityFacet.class);
        int seaLevel = region.getRegionFacet(SeaLevelFacet.class).getSeaLevel();

        float[] surfaceHeights = elevation.getInternal();
        float[] riversData = rivers.getInternal();
        float[] steepnessData = steepnessNoise.noise(elevation.getWorldArea(), scale);
        Biome[] biomeData = biomes.getInternal();
        float[] humidityData = humidityFacet.getInternal();
        Iterator<Vector2ic> positions = elevation.getWorldArea().iterator();
        for (int i = 0; i < surfaceHeights.length; ++i) {
            float steepness = TeraMath.clamp(steepnessData[i]);
            float riverFac = TeraMath.clamp(riversData[i]);

            // The river bed height is calculated as the sum of two curves, one for below the water and one above
            // Both are adjusted based on steepness, and the maximum depth also depends on the steepness
            // You can see the details on this graph: https://www.desmos.com/calculator/ahmr6ttsye
            float narrowness = 10;
            float riverBedHigh = 1 - Math.abs((1 + steepness) * riverFac - steepness);
            float lowFac = 0.2f - riverBedHigh + 0.9f * steepness;
            float riverBedLow = lowFac * TeraMath.fadePerlin(TeraMath.clamp(narrowness * (riverFac - 1) + 1));
            float riverBedElevation = seaLevel + rivers.maxDepth * (riverBedHigh - riverBedLow);

            float humidityAdj = Math.max(0, 15 * (0.4f - humidityData[i]));
            riverBedElevation += rivers.maxDepth * humidityAdj;

            // Never raise the surface to the river bed, erosion only goes downward
            if (riverBedElevation < surfaceHeights[i]) {
                riverFac = (riverFac - 0.8f * steepness) / (1 - 0.8f * steepness);
                riverFac = TeraMath.clamp(riverFac);
                riverFac = TeraMath.fadePerlin(riverFac);
                surfaceHeights[i] = TeraMath.lerp(surfaceHeights[i], riverBedElevation, riverFac);
            }

            Vector2ic pos = positions.next();
            if (TeraMath.clamp(riversData[i]) - 0.2 * humidityAdj > 0.86 + 0.03 * whiteNoiseRiver.noise(pos.x() * scale, pos.y() * scale)) {
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
