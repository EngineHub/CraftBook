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
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;
import org.bukkit.util.Vector;
import org.enginehub.craftbook.AbstractCraftBookMechanic;
import org.enginehub.craftbook.util.EventUtil;

public class BoatImpactDamage extends AbstractCraftBookMechanic {

    private static final Vector HALF_BLOCK_UP = new Vector(0, 0.5, 0);

    @EventHandler(priority = EventPriority.HIGH)
    public void onVehicleEntityCollision(VehicleEntityCollisionEvent event) {
        if (!EventUtil.passesFilter(event)) {
            return;
        }

        if (!(event.getVehicle() instanceof Boat) || event.getVehicle().isEmpty()) {
            return;
        }

        if (event.getEntity() instanceof LivingEntity) {
            if (event.getEntity().isInsideVehicle()) {
                return;
            }

            // It's impossible to determine the velocity, so just deal constant damage
            ((LivingEntity) event.getEntity()).damage(5);

            try {
                event.getEntity().setVelocity(event.getVehicle().getLocation().getDirection().normalize().multiply(1.2).add(HALF_BLOCK_UP));
            } catch (IllegalArgumentException e) {
                event.getEntity().setVelocity(HALF_BLOCK_UP);
            }
        } else if (removeOtherBoats && event.getEntity() instanceof Boat && event.getEntity().isEmpty()) {
            event.getEntity().remove();
        } else {
            return;
        }

        event.setCancelled(true);
        event.setPickupCancelled(true);
        event.setCollisionCancelled(true);
    }

    private boolean removeOtherBoats;

    @Override
    public void loadFromConfiguration(YAMLProcessor config) {
        config.setComment("remove-other-boats", "Allows boats to remove other boats on impact.");
        removeOtherBoats = config.getBoolean("remove-other-boats", false);
    }
}
