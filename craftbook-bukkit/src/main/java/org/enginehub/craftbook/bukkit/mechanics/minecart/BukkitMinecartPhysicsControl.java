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

package org.enginehub.craftbook.bukkit.mechanics.minecart;

import org.bukkit.entity.Minecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import org.bukkit.util.Vector;
import org.enginehub.craftbook.mechanic.CraftBookMechanic;
import org.enginehub.craftbook.mechanic.MechanicType;
import org.enginehub.craftbook.mechanics.minecart.MinecartPhysicsControl;
import org.enginehub.craftbook.util.EventUtil;

public class BukkitMinecartPhysicsControl extends MinecartPhysicsControl implements Listener {
    private Vector fallSpeed;
    private Vector derailedVelocityMod;

    public BukkitMinecartPhysicsControl(MechanicType<? extends CraftBookMechanic> mechanicType) {
        super(mechanicType);
    }

    @Override
    public void enable() {
        this.fallSpeed = new Vector(horizontalFallSpeed, verticalFallSpeed, horizontalFallSpeed);
        this.derailedVelocityMod = new Vector(offRailSpeed, offRailSpeed, offRailSpeed);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onVehicleCreate(VehicleCreateEvent event) {
        if (!EventUtil.passesFilter(event)) {
            return;
        }

        if (!(event.getVehicle() instanceof Minecart cart)) {
            return;
        }

        cart.setSlowWhenEmpty(slowWhenEmpty);

        if (verticalFallSpeed != -1 && horizontalFallSpeed != -1) {
            cart.setFlyingVelocityMod(fallSpeed);
        }

        if (offRailSpeed != -1) {
            cart.setDerailedVelocityMod(derailedVelocityMod);
        }

        if (maxSpeed != -1) {
            cart.setMaxSpeed(maxSpeed);
        }
    }
}
