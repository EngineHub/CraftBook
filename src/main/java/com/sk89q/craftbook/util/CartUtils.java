package com.sk89q.craftbook.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.minecart.ExplosiveMinecart;
import org.bukkit.entity.minecart.HopperMinecart;
import org.bukkit.entity.minecart.PoweredMinecart;
import org.bukkit.entity.minecart.RideableMinecart;
import org.bukkit.entity.minecart.SpawnerMinecart;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;

public class CartUtils {

    public static void reverse(Minecart cart) {

        cart.setVelocity(cart.getVelocity().normalize().multiply(-1));
    }

    public static void stop(Minecart cart) {

        cart.setVelocity(new Vector(0, 0, 0));
    }

    public static void teleport(final Minecart cart, Location destination) {

        EntityType type = cart.getType();
        final Minecart toCart;

        if(type == EntityType.MINECART_CHEST) {
            toCart = cart.getWorld().spawn(destination, StorageMinecart.class);
            ((StorageMinecart)toCart).getInventory().setContents(((StorageMinecart) cart).getInventory().getContents());
        }
        else if(type == EntityType.MINECART_FURNACE)
            toCart = cart.getWorld().spawn(destination, PoweredMinecart.class);
        else if(type == EntityType.MINECART_HOPPER) {
            toCart = cart.getWorld().spawn(destination, HopperMinecart.class);
            ((HopperMinecart)toCart).getInventory().setContents(((HopperMinecart) cart).getInventory().getContents());
        }
        else if(type == EntityType.MINECART_MOB_SPAWNER)
            toCart = cart.getWorld().spawn(destination, SpawnerMinecart.class);
        else if(type == EntityType.MINECART_TNT)
            toCart = cart.getWorld().spawn(destination, ExplosiveMinecart.class);
        else
            toCart = cart.getWorld().spawn(destination, RideableMinecart.class);

        final Entity passenger = cart.getPassenger();
        if (passenger != null) {
            cart.eject();
            passenger.teleport(destination);
            Bukkit.getScheduler().runTask(CraftBookPlugin.inst(), new Runnable() {

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

    public static ItemStack getCartStack(Minecart cart) {

        if(cart instanceof RideableMinecart)
            return new ItemStack(Material.MINECART, 1);
        else if(cart instanceof StorageMinecart)
            return new ItemStack(Material.STORAGE_MINECART, 1);
        else if(cart instanceof PoweredMinecart)
            return new ItemStack(Material.POWERED_MINECART, 1);
        else if(cart instanceof ExplosiveMinecart)
            return new ItemStack(Material.EXPLOSIVE_MINECART, 1);
        else if(cart instanceof HopperMinecart)
            return new ItemStack(Material.HOPPER_MINECART, 1);

        return null;
    }
}