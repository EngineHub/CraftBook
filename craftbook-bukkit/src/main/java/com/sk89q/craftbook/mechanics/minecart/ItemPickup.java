/*
 * CraftBook Copyright (C) me4502 <https://matthewmiller.dev/>
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

package com.sk89q.craftbook.mechanics.minecart;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.util.EventUtil;
import com.sk89q.util.yaml.YAMLProcessor;
import org.bukkit.entity.Item;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;

public class ItemPickup extends AbstractCraftBookMechanic {

    @EventHandler(priority = EventPriority.HIGH)
    public void onVehicleEntityCollision(VehicleEntityCollisionEvent event) {

        if(!EventUtil.passesFilter(event)) return;

        if (event.getVehicle() instanceof StorageMinecart && event.getEntity() instanceof Item) {

            StorageMinecart cart = (StorageMinecart) event.getVehicle();
            Collection<ItemStack> leftovers = cart.getInventory().addItem(((Item) event.getEntity()).getItemStack()).values();
            if(leftovers.isEmpty())
                event.getEntity().remove();
            else
                ((Item) event.getEntity()).setItemStack(leftovers.toArray(new ItemStack[1])[0]);

            event.setCollisionCancelled(true);
        }
    }

    @Override
    public void loadFromConfiguration(YAMLProcessor config) {

    }
}