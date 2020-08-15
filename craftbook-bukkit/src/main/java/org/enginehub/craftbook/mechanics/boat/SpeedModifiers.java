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
import org.bukkit.entity.Boat;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import org.enginehub.craftbook.AbstractCraftBookMechanic;
import org.enginehub.craftbook.util.EventUtil;

public class SpeedModifiers extends AbstractCraftBookMechanic {

    @EventHandler(priority = EventPriority.HIGH)
    public void onVehicleCreate(VehicleCreateEvent event) {

        if (!EventUtil.passesFilter(event)) return;

        if (!(event.getVehicle() instanceof Boat)) return;

        if (maxSpeed > 0)
            ((Boat) event.getVehicle()).setMaxSpeed(((Boat) event.getVehicle()).getMaxSpeed() * maxSpeed);
        if (unnoccupiedDeceleration > 0)
            ((Boat) event.getVehicle()).setUnoccupiedDeceleration(((Boat) event.getVehicle()).getUnoccupiedDeceleration() * unnoccupiedDeceleration);
        if (occupiedDeceleration > 0)
            ((Boat) event.getVehicle()).setOccupiedDeceleration(((Boat) event.getVehicle()).getOccupiedDeceleration() * occupiedDeceleration);
    }

    private double maxSpeed;
    private double unnoccupiedDeceleration;
    private double occupiedDeceleration;

    @Override
    public void loadFromConfiguration(YAMLProcessor config) {

        config.setComment("max-speed", "Sets the maximum speed of a boat. 0.4D is normal maximum speed.");
        maxSpeed = config.getDouble("max-speed", 0.4D);

        config.setComment("unnoccupied-deceleration", "Sets the unnoccupied deceleration of a boat. -1 is disabled.");
        unnoccupiedDeceleration = config.getDouble("unnoccupied-deceleration", -1);

        config.setComment("occupied-deceleration", "Sets the occupied deceleration of a boat. 0.3 is normal occupied deceleration");
        occupiedDeceleration = config.getDouble("occupied-deceleration", 0.2);
    }
}