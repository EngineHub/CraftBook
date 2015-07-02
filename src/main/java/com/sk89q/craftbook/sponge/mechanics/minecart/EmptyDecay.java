package com.sk89q.craftbook.sponge.mechanics.minecart;

import org.spongepowered.api.data.manipulator.entity.PassengerData;
import org.spongepowered.api.entity.vehicle.minecart.Minecart;
import org.spongepowered.api.event.Subscribe;
import org.spongepowered.api.event.entity.EntityDismountEvent;

import com.sk89q.craftbook.sponge.CraftBookPlugin;
import com.sk89q.craftbook.sponge.mechanics.types.SpongeMechanic;

public class EmptyDecay extends SpongeMechanic {

    @Subscribe
    public void onVehicleExit(EntityDismountEvent event) {

        if (event.getDismounted() instanceof Minecart) {
            event.getGame().getScheduler().getTaskBuilder().delay(40L).execute(new MinecartDecay((Minecart) event.getDismounted())).submit(CraftBookPlugin.inst());
        }
    }

    public static class MinecartDecay implements Runnable {

        Minecart cart;

        public MinecartDecay(Minecart cart) {

            this.cart = cart;
        }

        @Override
        public void run() {

            if (!cart.getData(PassengerData.class).isPresent()) {
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
