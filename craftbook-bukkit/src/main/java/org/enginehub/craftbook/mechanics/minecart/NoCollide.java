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
import org.bukkit.entity.Minecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;
import org.enginehub.craftbook.AbstractCraftBookMechanic;
import org.enginehub.craftbook.util.EventUtil;

public class NoCollide extends AbstractCraftBookMechanic {

    @EventHandler(priority = EventPriority.HIGH)
    public void onCartCollision(VehicleEntityCollisionEvent event) {

        if (!EventUtil.passesFilter(event)) return;

        if (event.getVehicle() instanceof Minecart) {
            if (event.getVehicle().isEmpty() && !empty) return;
            if (!event.getVehicle().isEmpty() && !full) return;

            event.setCollisionCancelled(true);
        }
    }

    private boolean empty;
    private boolean full;

    @Override
    public void loadFromConfiguration(YAMLProcessor config) {

        config.setComment("empty-carts", "Enable No Collide for empty carts.");
        empty = config.getBoolean("empty-carts", true);

        config.setComment("full-carts", "Enable No Collide for occupied carts.");
        full = config.getBoolean("full-carts", false);
    }
}