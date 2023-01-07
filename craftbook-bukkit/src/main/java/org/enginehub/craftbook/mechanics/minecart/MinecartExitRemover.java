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

import com.sk89q.util.yaml.YAMLProcessor;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.entity.minecart.RideableMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.enginehub.craftbook.AbstractCraftBookMechanic;
import org.enginehub.craftbook.CraftBook;
import org.enginehub.craftbook.bukkit.CraftBookPlugin;
import org.enginehub.craftbook.mechanic.MechanicTypes;
import org.enginehub.craftbook.util.EventUtil;

import java.util.Optional;

public class MinecartExitRemover extends AbstractCraftBookMechanic {

    @EventHandler(priority = EventPriority.HIGH)
    public void onVehicleExit(final VehicleExitEvent event) {
        if (!EventUtil.passesFilter(event)) {
            return;
        }

        Vehicle vehicle = event.getVehicle();

        if (vehicle instanceof RideableMinecart cart) {
            if (cart.isDead() || !cart.isValid()) {
                return;
            }

            // Ignore temporary carts here, we don't want to handle them.
            Optional<TemporaryCart> temporaryCart = CraftBook.getInstance().getPlatform().getMechanicManager().getMechanic(MechanicTypes.TEMPORARY_CART);
            if (temporaryCart.isPresent()) {
                if (cart.getPersistentDataContainer().has(temporaryCart.get().getTemporaryCartKey(), PersistentDataType.BYTE)) {
                    return;
                }
            }

            Bukkit.getScheduler().runTask(
                CraftBookPlugin.inst(),
                new MinecartRemover(event.getExited(), cart)
            );
        }
    }

    private class MinecartRemover implements Runnable {
        private final LivingEntity passenger;
        private final RideableMinecart minecart;

        private MinecartRemover(LivingEntity passenger, RideableMinecart minecart) {
            this.passenger = passenger;
            this.minecart = minecart;
        }

        @Override
        public void run() {
            if (!minecart.isValid() || minecart.isDead() || !minecart.isEmpty()) {
                return;
            }

            if (giveItem) {
                ItemStack stack = new ItemStack(minecart.getMinecartMaterial(), 1);

                if (passenger instanceof Player player) {
                    if (!player.getInventory().addItem(stack).isEmpty()) {
                        passenger.getLocation().getWorld().dropItemNaturally(passenger.getLocation(), stack);
                    }
                } else if (passenger != null) {
                    passenger.getLocation().getWorld().dropItemNaturally(passenger.getLocation(), stack);
                } else {
                    minecart.getLocation().getWorld().dropItemNaturally(minecart.getLocation(), stack);
                }
            }

            minecart.remove();
        }
    }

    private boolean giveItem;

    @Override
    public void loadFromConfiguration(YAMLProcessor config) {
        config.setComment("give-item", "Sets whether to give the player the item back or not.");
        giveItem = config.getBoolean("give-item", true);
    }
}
