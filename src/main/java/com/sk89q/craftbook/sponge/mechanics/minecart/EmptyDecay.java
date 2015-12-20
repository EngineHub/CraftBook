package com.sk89q.craftbook.sponge.mechanics.minecart;

import com.me4502.modularframework.module.Module;
import com.sk89q.craftbook.sponge.CraftBookPlugin;
import com.sk89q.craftbook.sponge.mechanics.types.SpongeMechanic;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.manipulator.mutable.entity.PassengerData;
import org.spongepowered.api.entity.vehicle.minecart.Minecart;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.DismountEntityEvent;

@Module(moduleName = "MinecartEmptyDecay", onEnable="onInitialize", onDisable="onDisable")
public class EmptyDecay extends SpongeMechanic {

    @Listener
    public void onVehicleExit(DismountEntityEvent event) {

        if (event.getTargetEntity() instanceof Minecart) {
            Sponge.getGame().getScheduler().createTaskBuilder().delayTicks(40L).execute(new MinecartDecay((Minecart) event.getTargetEntity())).submit(CraftBookPlugin.inst());
        }
    }

    public static class MinecartDecay implements Runnable {

        Minecart cart;

        public MinecartDecay(Minecart cart) {

            this.cart = cart;
        }

        @Override
        public void run() {
            if (!cart.get(PassengerData.class).isPresent()) {
                cart.remove();
            }
        }
    }

    @Override
    public String getName() {
        return "Minecart" + super.getName();
    }

    @Override
    public void onInitialize() {
    }
}
