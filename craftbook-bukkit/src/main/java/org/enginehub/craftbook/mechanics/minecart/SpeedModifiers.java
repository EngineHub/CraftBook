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

package com.sk89q.craftbook.mechanics.minecart;

import org.bukkit.entity.Minecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import org.bukkit.util.Vector;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.util.EventUtil;
import com.sk89q.util.yaml.YAMLProcessor;

public class SpeedModifiers extends AbstractCraftBookMechanic {

    @EventHandler(priority = EventPriority.HIGH)
    public void onVehicleCreate(VehicleCreateEvent event) {

        if(!EventUtil.passesFilter(event)) return;

        if (!(event.getVehicle() instanceof Minecart)) return;

        if (offRail > 0)
            ((Minecart) event.getVehicle()).setDerailedVelocityMod(new Vector(offRail, offRail, offRail));
        if(maxSpeed != 1)
            ((Minecart) event.getVehicle()).setMaxSpeed(((Minecart) event.getVehicle()).getMaxSpeed() * maxSpeed);
    }

    private double maxSpeed;
    private double offRail;

    @Override
    public void loadFromConfiguration(YAMLProcessor config) {

        config.setComment("max-speed", "Sets the max speed modifier of carts. Normal max speed speed is 0.4D");
        maxSpeed = config.getDouble("max-speed", 1);

        config.setComment("off-rail-speed", "Sets the off-rail speed modifier of carts. 0 is none.");
        offRail = config.getDouble("off-rail-speed", 0);
    }
}