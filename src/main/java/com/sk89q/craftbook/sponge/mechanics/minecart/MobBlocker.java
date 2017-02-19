package com.sk89q.craftbook.sponge.mechanics.minecart;

import com.me4502.modularframework.module.Module;
import com.sk89q.craftbook.core.util.documentation.DocumentationProvider;
import com.sk89q.craftbook.sponge.mechanics.types.SpongeMechanic;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.vehicle.minecart.RideableMinecart;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.entity.RideEntityEvent;
import org.spongepowered.api.event.filter.cause.Named;

@Module(id = "minecartmobblocker", name = "MinecartMobBlocker", onEnable="onInitialize", onDisable="onDisable")
public class MobBlocker extends SpongeMechanic implements DocumentationProvider {

    @Listener
    public void onVehicleEnter(RideEntityEvent.Mount event, @Named(NamedCause.SOURCE) Entity entity) {
        if (event.getTargetEntity() instanceof RideableMinecart) {
            if (!(entity instanceof Player)) {
                event.setCancelled(true);
            }
        }
    }

    @Override
    public String getPath() {
        return "mechanics/minecart/mob_blocker";
    }
}
