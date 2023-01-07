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

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.enginehub.craftbook.AbstractCraftBookMechanic;
import org.enginehub.craftbook.util.EventUtil;

public class MinecartRailPlacer extends AbstractCraftBookMechanic {

    @EventHandler(priority = EventPriority.HIGH)
    public void onVehicleMove(VehicleMoveEvent event) {
        if (!EventUtil.passesFilter(event)) {
            return;
        }

        Vehicle vehicle = event.getVehicle();

        if (!(vehicle instanceof InventoryHolder holder) || !(vehicle instanceof Minecart)) {
            return;
        }

        Block toBlock = event.getTo().getBlock();
        Block belowBlock = toBlock.getRelative(BlockFace.DOWN);

        if (toBlock.getType().isAir() && belowBlock.getType().isSolid()) {
            if (holder.getInventory().removeItem(new ItemStack(Material.RAIL, 1)).isEmpty()) {
                toBlock.setType(Material.RAIL);
            }
        }
    }
}
