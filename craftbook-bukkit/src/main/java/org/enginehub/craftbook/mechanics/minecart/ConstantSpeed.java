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
import org.bukkit.Material;
import org.bukkit.entity.Minecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.util.Vector;
import org.enginehub.craftbook.AbstractCraftBookMechanic;
import org.enginehub.craftbook.util.EventUtil;
import org.enginehub.craftbook.util.RailUtil;

public class ConstantSpeed extends AbstractCraftBookMechanic {

    @EventHandler(priority = EventPriority.HIGH)
    public void onVehicleMove(VehicleMoveEvent event) {

        if (!EventUtil.passesFilter(event)) return;

        if (!(event.getVehicle() instanceof Minecart)) return;

        if (RailUtil.isTrack(event.getTo().getBlock().getType()) && event.getVehicle().getVelocity().lengthSquared() > 0) {
            if (event.getTo().getBlock().getType() == Material.POWERED_RAIL && !ignorePoweredRail) {
                if ((event.getTo().getBlock().getData() & 8) == 0) {
                    return;
                }
            }
            Vector vel = event.getVehicle().getVelocity();
            event.getVehicle().setVelocity(vel.normalize().multiply(speed));
        }
    }

    private double speed;
    private boolean ignorePoweredRail;

    @Override
    public void loadFromConfiguration(YAMLProcessor config) {

        config.setComment("speed", "Sets the speed to move at constantly.");
        speed = config.getDouble("speed", 0.5);

        config.setComment("ignore-powered-rail", "Whether or not powered rails should be ignored.");
        ignorePoweredRail = config.getBoolean("ignore-powered-rail", false);
    }
}