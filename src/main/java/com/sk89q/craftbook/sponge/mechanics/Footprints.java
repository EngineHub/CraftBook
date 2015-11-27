package com.sk89q.craftbook.sponge.mechanics;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.me4502.modularframework.module.Module;
import com.sk89q.craftbook.sponge.mechanics.types.SpongeMechanic;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.particle.ParticleTypes;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.DisplaceEntityEvent;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Module(moduleName = "Footprints", onEnable="onInitialize", onDisable="onDisable")
public class Footprints extends SpongeMechanic {

    ParticleEffect footprintParticle;

    public void onInitialize() {
        footprintParticle = Sponge.getGame().getRegistry().createBuilder(ParticleEffect.Builder.class).type(ParticleTypes.FOOTSTEP).build();
    }

    private LoadingCache<UUID, FootprintData> footprintDataCache = CacheBuilder.newBuilder().expireAfterWrite(1L, TimeUnit.MINUTES).build(new CacheLoader<UUID, FootprintData>() {
        @Override public FootprintData load(UUID key) throws Exception {
            return new FootprintData();
        }
    });

    @Listener
    public void onEntityMove(DisplaceEntityEvent.Move.TargetLiving event) {

        if(event.getTargetEntity().isOnGround()) {
            FootprintData data = getFootprintData(event.getTargetEntity().getUniqueId());
            if(data.canPlaceFootprint(event.getToTransform().getPosition())) {

                Vector3d footprintLocation = event.getToTransform().getPosition().add(0, 0.19, 0);
                //Flip these, it should 'roughly' rotate 90 or -90 degrees.
                Vector3d footOffset = new Vector3d(footprintLocation.getZ(), 0, footprintLocation.getX()).normalize().div(2);

                event.getTargetEntity().getWorld().spawnParticles(footprintParticle, footprintLocation.add(footOffset.mul(data.side ? -1 : 1)));
                data.position = event.getToTransform().getPosition();
                data.side = !data.side;
            }
        }
    }

    public FootprintData getFootprintData(UUID uuid) {
        return footprintDataCache.getUnchecked(uuid);
    }

    private class FootprintData {
        public Vector3d position;
        public boolean side;

        public FootprintData() {
            this.position = new Vector3d(0,0,0);
            this.side = false;
        }

        public boolean canPlaceFootprint(Vector3d currentPosition) {
            return position.distanceSquared(currentPosition) > 1;
        }
    }
}
