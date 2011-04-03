// $Id$
/*
 * Copyright (C) 2010, 2011 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

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
