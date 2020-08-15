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

package org.enginehub.craftbook.mechanics.minecart;

import com.sk89q.util.yaml.YAMLProcessor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.RideableMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.inventory.ItemStack;
import org.enginehub.craftbook.AbstractCraftBookMechanic;
import org.enginehub.craftbook.CraftBook;
import org.enginehub.craftbook.bukkit.CraftBookPlugin;
import org.enginehub.craftbook.mechanic.MechanicTypes;
import org.enginehub.craftbook.util.CartUtil;
import org.enginehub.craftbook.util.EntityUtil;
import org.enginehub.craftbook.util.EventUtil;

public class ExitRemover extends AbstractCraftBookMechanic {

    @EventHandler(priority = EventPriority.HIGH)
    public void onVehicleExit(final VehicleExitEvent event) {

        if (!(event.getVehicle() instanceof RideableMinecart)) return;
        if (event.getVehicle().isDead() || !event.getVehicle().isValid()) return;

        if (!EventUtil.passesFilter(event)) return;

        if (CraftBook.getInstance().getPlatform().getMechanicManager().isMechanicEnabled(MechanicTypes.MINECART_TEMPORARY_CART)) {
            if (CraftBook.getInstance().getPlatform().getMechanicManager().getMechanic(MechanicTypes.MINECART_TEMPORARY_CART).get().getMinecarts().contains(event.getVehicle()))
                return;
        }

        Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), () -> {

            if (event.getVehicle().isDead() || !event.getVehicle().isValid()) return;

            if (giveItem) {

                ItemStack stack = CartUtil.getCartStack((Minecart) event.getVehicle());

                if (event.getExited() instanceof Player) {
                    if (!((Player) event.getExited()).getInventory().addItem(stack).isEmpty() && ((Player) event.getExited()).getGameMode() != GameMode.CREATIVE)
                        event.getExited().getWorld().dropItemNaturally(event.getExited().getLocation(), stack);
                } else if (event.getExited() != null)
                    event.getExited().getWorld().dropItemNaturally(event.getExited().getLocation(), stack);
            }
            EntityUtil.killEntity(event.getVehicle());
        }, 2L);
    }

    private boolean giveItem;

    @Override
    public void loadFromConfiguration(YAMLProcessor config) {

        config.setComment("give-item", "Sets whether to give the player the item back or not.");
        giveItem = config.getBoolean("give-item", false);
    }
}