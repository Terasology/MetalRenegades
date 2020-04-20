/*
 * Copyright 2019 MovingBlocks
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
package org.terasology.metalrenegades.world.dynamic;

import org.terasology.math.geom.BaseVector2i;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2f;
import org.terasology.utilities.procedural.BrownianNoise;
import org.terasology.utilities.procedural.PerlinNoise;
import org.terasology.utilities.procedural.SubSampledNoise;
import org.terasology.world.generation.Border3D;
import org.terasology.world.generation.FacetProvider;
import org.terasology.world.generation.GeneratingRegion;
import org.terasology.world.generation.Produces;
import org.terasology.world.generation.facets.SurfaceTemperatureFacet;

import java.util.Random;

@Produces(SurfaceTemperatureFacet.class)
public class TemperatureProvider implements FacetProvider {
    private  static final int SAMPLE_RATE = 4;

    private SubSampledNoise noise;
    private float[] temperatures;

    @Override
    public void setSeed(long seed) {
        noise = new SubSampledNoise(new BrownianNoise(new PerlinNoise(seed + 5), 8), new Vector2f(0.0005f, 0.0005f), SAMPLE_RATE);
    }

    @Override
    public void initialize() {

    }

    @Override
    public void process(GeneratingRegion region) {
        Border3D border = region.getBorderForFacet(SurfaceTemperatureFacet.class);
        SurfaceTemperatureFacet facet = new SurfaceTemperatureFacet(region.getRegion(), border);

        // TODO: Set temperature
        Rect2i processRegion = facet.getWorldRegion();
        for (BaseVector2i position: processRegion.contents()) {
            double temp = getRandomTemp(position);
            facet.setWorld(position, (float) temp);
        }

        region.setRegionFacet(SurfaceTemperatureFacet.class, facet);
    }

    private float generateWeight() {
        return 0;
    }

    private double getRandomTemp(BaseVector2i pos) {
//        float sumOfWeights = 10; // TODO
        Random random = new Random();
////        float f = random.nextFloat() * sumOfWeights;
        return Math.pow(random.nextFloat(),0.1);
//        return 0.6f;
    }
}
