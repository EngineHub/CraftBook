/*
 * CraftBook Copyright (C) me4502 <https://matthewmiller.dev/>
 * CraftBook Copyright (C) EngineHub and Contributors <https://enginehub.org/>
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not,
 * see <http://www.gnu.org/licenses/>.
 */
package org.enginehub.craftbook.sponge.mechanics;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.me4502.modularframework.module.Module;
import com.me4502.modularframework.module.guice.ModuleConfiguration;
import org.enginehub.craftbook.core.util.ConfigValue;
import org.enginehub.craftbook.core.util.CraftBookException;
import org.enginehub.craftbook.core.util.documentation.DocumentationProvider;
import org.enginehub.craftbook.sponge.mechanics.types.SpongeMechanic;
import org.enginehub.craftbook.sponge.util.SpongeBlockFilter;
import org.enginehub.craftbook.sponge.util.BlockUtil;
import org.enginehub.craftbook.sponge.util.type.TypeTokens;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.particle.ParticleTypes;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.util.Direction;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

@Module(id = "footprints", name = "Footprints", onEnable="onInitialize", onDisable="onDisable")
public class Footprints extends SpongeMechanic implements DocumentationProvider {

    @Inject
    @ModuleConfiguration
    public ConfigurationNode config;

    private ConfigValue<List<SpongeBlockFilter>>
            allowedBlocks = new ConfigValue<>("allowed-blocks", "A list of blocks that can have footprints on.", getDefaultBlocks(), new TypeTokens.BlockFilterListTypeToken());

    private ParticleEffect footprintParticle;

    @Override
    public void onInitialize() throws CraftBookException {
        super.onInitialize();

        footprintParticle = Sponge.getGame().getRegistry().createBuilder(ParticleEffect.Builder.class).type(ParticleTypes.FOOTSTEP).build();

        allowedBlocks.load(config);
    }

    private LoadingCache<UUID, FootprintData> footprintDataCache = CacheBuilder.newBuilder().expireAfterAccess(1L, TimeUnit.MINUTES).build(new FootprintDataCacheLoader());

    @Listener
    public void onEntityMove(MoveEntityEvent event) {
        if(event.getTargetEntity().isOnGround()) {
            if(BlockUtil.doesStatePassFilters(allowedBlocks.getValue(), event.getTargetEntity().getLocation().getRelative(Direction.DOWN).getBlock())) {
                FootprintData data = getFootprintData(event.getTargetEntity().getUniqueId());
                if (data.canPlaceFootprint(event.getToTransform().getPosition())) {
                    Vector3d footprintLocation = event.getToTransform().getPosition().add(0, 0.19, 0);
                    //Flip these, it should 'roughly' rotate 90 or -90 degrees.
                    Vector3d footOffset = new Vector3d(footprintLocation.getZ(), 0, footprintLocation.getX()).normalize().div(2);

                    event.getTargetEntity().getWorld().spawnParticles(footprintParticle, footprintLocation.add(footOffset.mul(data.side ? -1 : 1)));
                    data.position = event.getToTransform().getPosition();
                    data.side = !data.side;
                }
            }
        }
    }

    private static List<SpongeBlockFilter> getDefaultBlocks() {
        List<SpongeBlockFilter> states = Lists.newArrayList();
        states.add(new SpongeBlockFilter(BlockTypes.SAND));
        states.add(new SpongeBlockFilter(BlockTypes.DIRT));
        states.add(new SpongeBlockFilter(BlockTypes.GRAVEL));
        states.add(new SpongeBlockFilter(BlockTypes.SNOW));
        states.add(new SpongeBlockFilter(BlockTypes.SNOW_LAYER));
        states.add(new SpongeBlockFilter(BlockTypes.GRASS));
        states.add(new SpongeBlockFilter(BlockTypes.GRASS_PATH));
        return states;
    }

    private FootprintData getFootprintData(UUID uuid) {
        return footprintDataCache.getUnchecked(uuid);
    }

    @Override
    public String getPath() {
        return "mechanics/footprints";
    }

    @Override
    public ConfigValue<?>[] getConfigurationNodes() {
        return new ConfigValue<?>[] {
                allowedBlocks
        };
    }

    private static class FootprintData {
        Vector3d position;
        boolean side;

        FootprintData() {
            this.position = new Vector3d(0,0,0);
            this.side = false;
        }

        boolean canPlaceFootprint(Vector3d currentPosition) {
            return position.distanceSquared(currentPosition) > 1;
        }
    }

    private static class FootprintDataCacheLoader extends CacheLoader<UUID, FootprintData> {
        @Override
        public FootprintData load(@Nonnull UUID key) throws Exception {
            return new FootprintData();
        }
    }
}
