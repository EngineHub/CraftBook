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
import org.bukkit.entity.Entity;
import org.bukkit.entity.minecart.RideableMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.enginehub.craftbook.AbstractCraftBookMechanic;
import org.enginehub.craftbook.bukkit.CraftBookPlugin;
import org.enginehub.craftbook.mechanic.CraftBookMechanic;
import org.enginehub.craftbook.mechanic.MechanicType;
import org.enginehub.craftbook.util.EventUtil;

public class MinecartEmptyDecay extends AbstractCraftBookMechanic {

    public MinecartEmptyDecay(MechanicType<? extends CraftBookMechanic> mechanicType) {
        super(mechanicType);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onVehicleExit(VehicleExitEvent event) {
        if (!EventUtil.passesFilter(event)) {
            return;
        }

        if (!(event.getVehicle() instanceof RideableMinecart rideableMinecart)) {
            return;
        }

        Bukkit.getScheduler().runTaskLater(
            CraftBookPlugin.inst(),
            new Decay(rideableMinecart),
            decayDelay
        );
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onChunkLoad(ChunkLoadEvent event) {
        if (!EventUtil.passesFilter(event)) {
            return;
        }

        for (Entity ent : event.getChunk().getEntities()) {
            if (ent instanceof RideableMinecart cart) {
                if (!cart.isValid()) {
                    continue;
                }
                if (!cart.isEmpty()) {
                    continue;
                }

                Bukkit.getScheduler().runTaskLater(
                    CraftBookPlugin.inst(),
                    new Decay(cart),
                    decayDelay
                );
            }
        }
    }

    private record Decay(RideableMinecart cart) implements Runnable {

        @Override
        public void run() {
            if (cart == null || !cart.isValid() || !cart.isEmpty()) {
                return;
            }

            cart.remove();
        }
    }

    private int decayDelay;

    @Override
    public void loadFromConfiguration(YAMLProcessor config) {
        config.setComment("decay-delay", "The time in ticks that the cart will wait before decaying.");
        decayDelay = config.getInt("decay-delay", 200);
    }
}
