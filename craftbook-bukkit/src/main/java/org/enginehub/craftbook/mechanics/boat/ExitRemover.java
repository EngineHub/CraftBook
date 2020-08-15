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

package org.enginehub.craftbook.mechanics.boat;

import com.sk89q.util.yaml.YAMLProcessor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Boat;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.inventory.ItemStack;
import org.enginehub.craftbook.AbstractCraftBookMechanic;
import org.enginehub.craftbook.bukkit.CraftBookPlugin;
import org.enginehub.craftbook.util.EntityUtil;
import org.enginehub.craftbook.util.EventUtil;
import org.enginehub.craftbook.util.ItemUtil;

public class ExitRemover extends AbstractCraftBookMechanic {

    @EventHandler(priority = EventPriority.HIGH)
    public void onVehicleExit(VehicleExitEvent event) {

        if (!EventUtil.passesFilter(event)) return;

        if (!(event.getVehicle() instanceof Boat)) return;

        Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), new BoatRemover(event.getExited(), (Boat) event.getVehicle()), 2L);
    }

    class BoatRemover implements Runnable {

        LivingEntity player;
        Boat boat;

        BoatRemover(LivingEntity player, Boat boat) {
            this.player = player;
            this.boat = boat;
        }

        @Override
        public void run() {

            if (!boat.isValid() || boat.isDead() || !boat.isEmpty()) return;

            if (giveItem) {
                ItemStack stack = new ItemStack(ItemUtil.getBoatFromTree(boat.getWoodType()), 1);

                if (player instanceof Player) {
                    if (!((Player) player).getInventory().addItem(stack).isEmpty())
                        player.getLocation().getWorld().dropItemNaturally(player.getLocation(), stack);
                } else if (player != null)
                    player.getLocation().getWorld().dropItemNaturally(player.getLocation(), stack);
                else
                    boat.getLocation().getWorld().dropItemNaturally(boat.getLocation(), stack);
            }
            EntityUtil.killEntity(boat);
        }
    }

    boolean giveItem;

    @Override
    public void loadFromConfiguration(YAMLProcessor config) {

        config.setComment("give-item", "Sets whether to give the player the item back or not.");
        giveItem = config.getBoolean("give-item", false);
    }
}