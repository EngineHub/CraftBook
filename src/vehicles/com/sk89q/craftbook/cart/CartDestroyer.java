package com.sk89q.craftbook.cart;

import org.bukkit.entity.*;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.Location;
import org.bukkit.World;

/**
 * Mechanism that removes carts and optionally drops the item when
 * a player (or other living entity) leaves the cart.
 *
 * @author wizjany
 */

public class CartDestroyer {
    public void destroyCart(VehicleExitEvent event, Boolean creature, Boolean drop) {

        Minecart minecart = (Minecart) event.getVehicle();
        LivingEntity entity = event.getExited();
        //destroy cart; creature being true means we SHOULD delete a creature occupied cart
        if (!creature && entity instanceof Creature) {
            return;
        } else {
            minecart.remove();
        }
        //drop cart if we're supposed to
        if (drop) {
            Location location = entity.getLocation();
            ItemStack cartdrop = new ItemStack(Material.MINECART, 1);
            location.getWorld().dropItemNaturally(location, cartdrop);
        }
    }
}
