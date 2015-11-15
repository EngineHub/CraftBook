package com.sk89q.craftbook.sponge.mechanics;

import com.me4502.modularframework.module.Module;
import com.sk89q.craftbook.sponge.mechanics.types.SpongeMechanic;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.particle.ParticleTypes;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.DisplaceEntityEvent;

@Module(moduleName = "Footprints", onEnable="onInitialize", onDisable="onDisable")
public class Footprints extends SpongeMechanic {

    @Listener
    public void onEntityMove(DisplaceEntityEvent.Move.TargetLiving event) {

        if (event.getFromTransform().getPosition().toInt().equals(event.getToTransform().getPosition().toInt())) return;

        // TODO make this only place footprints every so often, and only on certain blocks.
        if(event.getTargetEntity().isOnGround())
            event.getTargetEntity().getWorld().spawnParticles(event.getGame().getRegistry().createBuilder(ParticleEffect.Builder.class).type(ParticleTypes.FOOTSTEP).build(), event.getTargetEntity().getLocation().getPosition().add(0,0.19,0));
    }
}
