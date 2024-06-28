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

package org.enginehub.craftbook.mechanics.minecart;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.enginehub.craftbook.AbstractCraftBookMechanic;
import org.enginehub.craftbook.mechanic.CraftBookMechanic;
import org.enginehub.craftbook.mechanic.MechanicType;
import org.enginehub.craftbook.util.EventUtil;

import java.util.Collection;

public class MinecartItemPickup extends AbstractCraftBookMechanic {

    public MinecartItemPickup(MechanicType<? extends CraftBookMechanic> mechanicType) {
        super(mechanicType);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onVehicleMove(VehicleMoveEvent event) {
        if (!EventUtil.passesFilter(event)) {
            return;
        }

        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
            && event.getFrom().getBlockZ() == event.getTo().getBlockZ()
            && event.getFrom().getBlockY() == event.getTo().getBlockY()) {
            return;
        }

        Vehicle vehicle = event.getVehicle();

        if (vehicle instanceof Minecart
            && vehicle instanceof InventoryHolder) {
            Inventory inventory = ((InventoryHolder) vehicle).getInventory();

            for (Entity entity : vehicle.getNearbyEntities(1, 1, 1)) {
                if (entity instanceof Item item) {
                    // Safer to do this per-item. Shouldn't be an issue as carts will not hit millions
                    // of items at a time
                    Collection<ItemStack> leftovers = inventory.addItem(item.getItemStack()).values();

                    if (leftovers.isEmpty()) {
                        item.remove();
                    } else {
                        item.setItemStack(leftovers.toArray(new ItemStack[0])[0]);
                    }
                }
            }
        }
    }
}
