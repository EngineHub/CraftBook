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

import org.bukkit.entity.Boat;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;
import org.bukkit.util.Vector;
import org.enginehub.craftbook.mechanic.CraftBookMechanic;
import org.enginehub.craftbook.mechanic.MechanicType;
import org.enginehub.craftbook.mechanics.boat.BoatImpactDamage;
import org.enginehub.craftbook.util.EventUtil;

public class BukkitBoatImpactDamage extends BoatImpactDamage implements Listener {

    private static final Vector HALF_BLOCK_UP = new Vector(0, 0.5, 0);

    public BukkitBoatImpactDamage(MechanicType<? extends CraftBookMechanic> mechanicType) {
        super(mechanicType);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onVehicleEntityCollision(VehicleEntityCollisionEvent event) {
        if (!EventUtil.passesFilter(event)) {
            return;
        }

        if (!(event.getVehicle() instanceof Boat) || event.getVehicle().isEmpty()) {
            return;
        }

        if (event.getEntity() instanceof LivingEntity living) {
            if (living.isInsideVehicle()) {
                return;
            }

            // It's impossible to determine the velocity, so just deal constant damage
            living.damage(5);

            try {
                living.setVelocity(living.getLocation().getDirection().normalize().multiply(1.2).add(HALF_BLOCK_UP));
            } catch (IllegalArgumentException e) {
                living.setVelocity(HALF_BLOCK_UP);
            }
        } else if (removeOtherBoats && event.getEntity() instanceof Boat && event.getEntity().isEmpty()) {
            event.getEntity().remove();
        } else {
            return;
        }

        event.setCancelled(true);
    }
}
