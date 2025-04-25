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

package org.enginehub.craftbook.bukkit.mechanics.boat;

import org.bukkit.Bukkit;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.enginehub.craftbook.bukkit.CraftBookPlugin;
import org.enginehub.craftbook.mechanic.CraftBookMechanic;
import org.enginehub.craftbook.mechanic.MechanicType;
import org.enginehub.craftbook.mechanics.boat.BoatEmptyDecay;
import org.enginehub.craftbook.util.EventUtil;

public class BukkitBoatEmptyDecay extends BoatEmptyDecay implements Listener {

    public BukkitBoatEmptyDecay(MechanicType<? extends CraftBookMechanic> mechanicType) {
        super(mechanicType);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onVehicleExit(VehicleExitEvent event) {
        if (!EventUtil.passesFilter(event)) {
            return;
        }

        if (!(event.getVehicle() instanceof Boat boat)) {
            return;
        }

        Bukkit.getScheduler().runTaskLater(
            CraftBookPlugin.inst(),
            new Decay(boat),
            decayDelay
        );
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onChunkLoad(ChunkLoadEvent event) {
        if (!EventUtil.passesFilter(event)) {
            return;
        }

        for (Entity ent : event.getChunk().getEntities()) {
            if (ent instanceof Boat boat) {
                if (!boat.isValid()) {
                    continue;
                }
                if (!boat.isEmpty()) {
                    continue;
                }

                Bukkit.getScheduler().runTaskLater(
                    CraftBookPlugin.inst(),
                    new Decay(boat),
                    decayDelay
                );
            }
        }
    }

    private record Decay(Boat boat) implements Runnable {

        @Override
        public void run() {
            if (!boat.isValid() || !boat.isEmpty()) {
                return;
            }

            boat.remove();
        }
    }
}
