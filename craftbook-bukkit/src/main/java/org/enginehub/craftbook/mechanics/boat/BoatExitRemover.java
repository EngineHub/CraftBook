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

package org.enginehub.craftbook.mechanics.boat;

import com.sk89q.util.yaml.YAMLProcessor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Boat;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.inventory.ItemStack;
import org.enginehub.craftbook.AbstractCraftBookMechanic;
import org.enginehub.craftbook.bukkit.CraftBookPlugin;
import org.enginehub.craftbook.mechanic.CraftBookMechanic;
import org.enginehub.craftbook.mechanic.MechanicType;
import org.enginehub.craftbook.util.EventUtil;

public class BoatExitRemover extends AbstractCraftBookMechanic implements Listener {

    public BoatExitRemover(MechanicType<? extends CraftBookMechanic> mechanicType) {
        super(mechanicType);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onVehicleExit(VehicleExitEvent event) {
        if (!EventUtil.passesFilter(event)) {
            return;
        }

        Vehicle vehicle = event.getVehicle();

        if (vehicle instanceof Boat boat) {
            if (boat.isDead() || !boat.isValid()) {
                return;
            }

            Bukkit.getScheduler().runTask(
                CraftBookPlugin.inst(),
                new BoatRemover(event.getExited(), boat)
            );
        }
    }

    private class BoatRemover implements Runnable {
        private final LivingEntity passenger;
        private final Boat boat;

        private BoatRemover(LivingEntity passenger, Boat boat) {
            this.passenger = passenger;
            this.boat = boat;
        }

        @Override
        public void run() {
            if (!boat.isValid() || boat.isDead() || !boat.isEmpty()) {
                return;
            }

            if (giveItem) {
                ItemStack stack = new ItemStack(boat.getBoatMaterial(), 1);

                if (passenger instanceof Player player) {
                    if (!player.getInventory().addItem(stack).isEmpty()) {
                        passenger.getLocation().getWorld().dropItemNaturally(passenger.getLocation(), stack);
                    }
                } else if (passenger.isValid()) {
                    passenger.getLocation().getWorld().dropItemNaturally(passenger.getLocation(), stack);
                } else {
                    boat.getLocation().getWorld().dropItemNaturally(boat.getLocation(), stack);
                }
            }

            boat.remove();
        }
    }

    boolean giveItem;

    @Override
    public void loadFromConfiguration(YAMLProcessor config) {
        config.setComment("give-item", "Sets whether to give the player the item back or not.");
        giveItem = config.getBoolean("give-item", true);
    }
}
