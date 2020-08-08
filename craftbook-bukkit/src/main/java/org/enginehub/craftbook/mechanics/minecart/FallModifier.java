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
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.util.Vector;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.util.EventUtil;
import com.sk89q.util.yaml.YAMLProcessor;

public class FallModifier extends AbstractCraftBookMechanic {

    @Override
    public void disable() {
        fallSpeed = null;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onVehicleCreate(VehicleCreateEvent event) {

        if(!EventUtil.passesFilter(event)) return;

        if (!(event.getVehicle() instanceof Minecart)) return;

        ((Minecart) event.getVehicle()).setFlyingVelocityMod(fallSpeed);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onVehicleMove(VehicleMoveEvent event) {

        if(!EventUtil.passesFilter(event)) return;

        if (!(event.getVehicle() instanceof Minecart)) return;

        ((Minecart) event.getVehicle()).setFlyingVelocityMod(fallSpeed);
    }

    private double verticalSpeed;
    private double horizontalSpeed;
    private Vector fallSpeed;

    @Override
    public void loadFromConfiguration(YAMLProcessor config) {

        config.setComment("vertical-fall-speed", "Sets the vertical fall speed of the minecart");
        verticalSpeed = config.getDouble("vertical-fall-speed", 0.9D);

        config.setComment("horizontal-fall-speed", "Sets the horizontal fall speed of the minecart");
        horizontalSpeed = config.getDouble("horizontal-fall-speed", 1.1D);

        fallSpeed = new Vector(horizontalSpeed, verticalSpeed, horizontalSpeed);
    }
}