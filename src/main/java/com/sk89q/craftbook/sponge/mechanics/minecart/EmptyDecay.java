package com.sk89q.craftbook.sponge.mechanics.minecart;

import org.spongepowered.api.entity.vehicle.minecart.Minecart;
import org.spongepowered.api.event.entity.EntityDismountEvent;

import com.sk89q.craftbook.core.util.CachePolicy;
import com.sk89q.craftbook.sponge.CraftBookPlugin;
import com.sk89q.craftbook.sponge.mechanics.SpongeMechanic;

public class EmptyDecay extends SpongeMechanic {

    public void onVehicleExit(EntityDismountEvent event) {

        if(event.getDismounted() instanceof Minecart)
            event.getGame().getScheduler().runTaskAfter(CraftBookPlugin.<CraftBookPlugin>inst(), new MinecartDecay((Minecart) event.getDismounted()), 40L);
    }

    public static class MinecartDecay implements Runnable {

        Minecart cart;

        public MinecartDecay(Minecart cart) {

            this.cart = cart;
        }

        @Override
        public void run() {

            if(!cart.getPassenger().isPresent()) {
                cart.remove();
            }
        }
    }

    @Override
    public String getName () {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onInitialize () {
        // TODO Auto-generated method stub

    }

    @Override
    public CachePolicy getCachePolicy () {
        // TODO Auto-generated method stub
        return null;
    }
}