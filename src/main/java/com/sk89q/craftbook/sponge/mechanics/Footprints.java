package com.sk89q.craftbook.sponge.mechanics;

import org.spongepowered.api.effect.particle.ParticleTypes;
import org.spongepowered.api.event.entity.living.LivingMoveEvent;
import org.spongepowered.api.util.event.Subscribe;

public class Footprints extends SpongeMechanic {

    @Subscribe
    public void onEntityMove(LivingMoveEvent event) {

        if (event.getOldLocation().getBlockPosition().equals(event.getNewLocation().getBlockPosition())) return;

        // TODO make this only place footprints every so often, and only on certain blocks.
        event.getLiving().getWorld().spawnParticles(event.getGame().getRegistry().getParticleEffectBuilder(ParticleTypes.FOOTSTEP).build(), event.getLiving().getLocation().getPosition());
    }
}
