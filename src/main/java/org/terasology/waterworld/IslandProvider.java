/*
 * Copyright 2015 MovingBlocks
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

package org.terasology.waterworld;

import org.terasology.entitySystem.Component;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.BaseVector2i;
import org.terasology.math.geom.Vector2f;
import org.terasology.rendering.nui.properties.Range;
import org.terasology.utilities.procedural.BrownianNoise;
import org.terasology.utilities.procedural.Noise;
import org.terasology.utilities.procedural.PerlinNoise;
import org.terasology.utilities.procedural.SubSampledNoise;
import org.terasology.world.generation.*;
import org.terasology.world.generation.facets.SurfaceHeightFacet;

@Updates(@Facet(SurfaceHeightFacet.class))
public class IslandProvider implements ConfigurableFacetProvider {

    private Noise islandNoise;

    //Be sure to initialize this!
    private IslandConfiguration configuration = new IslandConfiguration();

    @Override
    public void setSeed(long seed) {
        islandNoise = new SubSampledNoise(new BrownianNoise(new PerlinNoise(seed + 2), 8), new Vector2f(0.001f, 0.001f), 1);
    }

    @Override
    public void process(GeneratingRegion region) {
        SurfaceHeightFacet facet = region.getRegionFacet(SurfaceHeightFacet.class);
        float islandHeight = configuration.islandHeight;
        // loop through every position on our 2d array
        Rect2i processRegion = facet.getWorldRegion();
        for (BaseVector2i position : processRegion.contents()) {
            // scale our max island height to noise (between -1 and 1)
            float additiveIslandHeight = islandNoise.noise(position.x(), position.y()) * islandHeight;
            // dont bother subtracting island height,  that will allow unaffected regions
            additiveIslandHeight = TeraMath.clamp(additiveIslandHeight, 0, islandHeight);

            facet.setWorld(position, facet.getWorld(position) + additiveIslandHeight);
        }
    }

    @Override
    public String getConfigurationName() {
        return "Islands";
    }

    @Override
    public Component getConfiguration() {
        return configuration;
    }

    @Override
    public void setConfiguration(Component configuration) {
        this.configuration = (IslandConfiguration) configuration;
    }

    private static class IslandConfiguration implements Component {
        @Range(min = 200, max = 500f, increment = 20f, precision = 1, description = "Mountain Height")
        private float islandHeight = 400;
    }
}