package com.sk89q.craftbook.sponge.mechanics;

import com.me4502.modularframework.module.Module;
import org.spongepowered.api.effect.particle.ParticleTypes;
import org.spongepowered.api.event.Subscribe;
import org.spongepowered.api.event.entity.living.LivingMoveEvent;

import com.sk89q.craftbook.sponge.mechanics.types.SpongeMechanic;

@Module(moduleName = "Footprints", onEnable="onInitialize", onDisable="onDisable")
public class Footprints extends SpongeMechanic {

    @Subscribe
    public void onEntityMove(LivingMoveEvent event) {

        if (event.getOldLocation().getBlockPosition().equals(event.getNewLocation().getBlockPosition())) return;

        // TODO make this only place footprints every so often, and only on certain blocks.
        if(event.getEntity().isOnGround())
            event.getEntity().getWorld().spawnParticles(event.getGame().getRegistry().getParticleEffectBuilder(ParticleTypes.FOOTSTEP).build(), event.getEntity().getLocation().getPosition().add(0,0.19,0));
    }
}
