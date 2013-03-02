package com.sk89q.craftbook.cart;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.util.Vector;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;

public abstract class CartUtils {

    public static void reverse(Minecart cart) {

        cart.setVelocity(cart.getVelocity().normalize().multiply(-1));
    }

    public static void stop(Minecart cart) {

        cart.setVelocity(new Vector(0, 0, 0));
    }

    public static void teleport(final Minecart cart, Location destination) {

        final Minecart toCart = cart.getWorld().spawn(destination, Minecart.class);
        final Entity passenger = cart.getPassenger();
        if (passenger != null) {
            cart.eject();
            passenger.teleport(destination);
            Bukkit.getScheduler().scheduleSyncDelayedTask(CraftBookPlugin.inst(), new Runnable() {

                @Override
                public void run() {

                    toCart.setPassenger(passenger);
                    passenger.setVelocity(cart.getVelocity());
                }
            });
        }
        toCart.getLocation().setYaw(cart.getLocation().getYaw());
        toCart.getLocation().setPitch(cart.getLocation().getPitch());
        toCart.setVelocity(cart.getVelocity()); // speedy thing goes in, speedy thing comes out
        cart.remove();
    }
}
