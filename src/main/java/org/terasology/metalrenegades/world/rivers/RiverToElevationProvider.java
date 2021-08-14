// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.metalrenegades.world.rivers;

import org.terasology.engine.world.generation.ConfigurableFacetProvider;
import org.terasology.engine.world.generation.Facet;
import org.terasology.engine.world.generation.GeneratingRegion;
import org.terasology.engine.world.generation.Requires;
import org.terasology.engine.world.generation.Updates;
import org.terasology.engine.world.generation.facets.ElevationFacet;
import org.terasology.engine.world.generation.facets.SeaLevelFacet;
import org.terasology.engine.world.generation.facets.SurfaceHumidityFacet;
import org.terasology.gestalt.entitysystem.component.Component;
import org.terasology.math.TeraMath;


@Requires({@Facet(RiverFacet.class), @Facet(SeaLevelFacet.class)})
@Updates({@Facet(ElevationFacet.class), @Facet(SurfaceHumidityFacet.class)})
public class RiverToElevationProvider implements ConfigurableFacetProvider {
    private Configuration configuration = new Configuration();

    @Override
    public void process(GeneratingRegion region) {
        RiverFacet rivers = region.getRegionFacet(RiverFacet.class);
        ElevationFacet elevation = region.getRegionFacet(ElevationFacet.class);
        SurfaceHumidityFacet humidity = region.getRegionFacet(SurfaceHumidityFacet.class);
        int seaLevel = region.getRegionFacet(SeaLevelFacet.class).getSeaLevel();

        float[] surfaceHeights = elevation.getInternal();
        float[] riversData = rivers.getInternal();
        float[] humidityData = humidity.getInternal();
        for (int i = 0; i < surfaceHeights.length; ++i) {
            float riverFac = TeraMath.clamp(riversData[i]);
            float riverBedElevation = seaLevel - rivers.maxDepth * (riversData[i] * 4 - 3);
            surfaceHeights[i] = surfaceHeights[i] * (1 - riverFac) + riverBedElevation * riverFac;
            humidityData[i] += Math.max(0, 0.1 * (seaLevel - riverBedElevation + 7) * riverFac);
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

    private static class Configuration implements Component<Configuration> {
        @Override
        public void copyFrom(Configuration other) {

        }
    }
}
