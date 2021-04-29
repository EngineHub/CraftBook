/*
 * CraftBook Copyright (C) EngineHub and Contributors <https://enginehub.org/>
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not,
 * see <http://www.gnu.org/licenses/>.
 */

package org.enginehub.craftbook.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.minecart.CommandMinecart;
import org.bukkit.entity.minecart.ExplosiveMinecart;
import org.bukkit.entity.minecart.HopperMinecart;
import org.bukkit.entity.minecart.PoweredMinecart;
import org.bukkit.entity.minecart.RideableMinecart;
import org.bukkit.entity.minecart.SpawnerMinecart;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.util.Vector;
import org.enginehub.craftbook.bukkit.CraftBookPlugin;

import java.util.List;

public final class CartUtil {

    private CartUtil() {
    }

    public static void stop(Minecart cart) {

        cart.setVelocity(new Vector(0, 0, 0));
    }

    public static void teleport(final Minecart cart, Location destination) {

        EntityType type = cart.getType();
        final Minecart toCart;

        if (type == EntityType.MINECART_CHEST) {
            toCart = cart.getWorld().spawn(destination, StorageMinecart.class);
            ((StorageMinecart) toCart).getInventory().setContents(((StorageMinecart) cart).getInventory().getContents());
            ((StorageMinecart) cart).getInventory().clear();
        } else if (type == EntityType.MINECART_FURNACE) {
            toCart = cart.getWorld().spawn(destination, PoweredMinecart.class);
        } else if (type == EntityType.MINECART_HOPPER) {
            toCart = cart.getWorld().spawn(destination, HopperMinecart.class);
            ((HopperMinecart) toCart).getInventory().setContents(((HopperMinecart) cart).getInventory().getContents());
            ((HopperMinecart) cart).getInventory().clear();
        } else if (type == EntityType.MINECART_MOB_SPAWNER) {
            toCart = cart.getWorld().spawn(destination, SpawnerMinecart.class);
        } else if (type == EntityType.MINECART_TNT)
            toCart = cart.getWorld().spawn(destination, ExplosiveMinecart.class);
        else if (type == EntityType.MINECART_COMMAND) {
            toCart = cart.getWorld().spawn(destination, CommandMinecart.class);
            ((CommandMinecart) toCart).setCommand(((CommandMinecart) toCart).getCommand());
            ((CommandMinecart) toCart).setName(toCart.getName());
        } else
            toCart = cart.getWorld().spawn(destination, RideableMinecart.class);

        final List<Entity> passengers = cart.getPassengers();
        if (!passengers.isEmpty()) {
            cart.eject();
            for (Entity passenger : passengers) {
                passenger.teleport(destination);
            }
            Bukkit.getScheduler().runTask(CraftBookPlugin.inst(), () -> {
                for (Entity passenger : passengers) {
                    toCart.addPassenger(passenger);
                    passenger.setVelocity(cart.getVelocity());
                }
            });
        }
        toCart.getLocation().setYaw(cart.getLocation().getYaw());
        toCart.getLocation().setPitch(cart.getLocation().getPitch());
        toCart.setVelocity(cart.getVelocity()); // speedy thing goes in, speedy thing comes out
        cart.remove();
    }

    public static boolean isMinecart(Material material) {
        switch (material) {
            case MINECART:
            case CHEST_MINECART:
            case COMMAND_BLOCK_MINECART:
            case FURNACE_MINECART:
            case HOPPER_MINECART:
            case TNT_MINECART:
                return true;
            default:
                return false;
        }
    }
}