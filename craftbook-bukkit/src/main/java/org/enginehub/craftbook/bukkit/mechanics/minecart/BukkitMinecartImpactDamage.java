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

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.RideableMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;
import org.bukkit.util.Vector;
import org.enginehub.craftbook.mechanic.CraftBookMechanic;
import org.enginehub.craftbook.mechanic.MechanicType;
import org.enginehub.craftbook.mechanics.minecart.MinecartImpactDamage;
import org.enginehub.craftbook.util.EventUtil;

public class BukkitMinecartImpactDamage extends MinecartImpactDamage implements Listener {

    private static final Vector HALF_BLOCK_UP = new Vector(0, 0.5, 0);

    public BukkitMinecartImpactDamage(MechanicType<? extends CraftBookMechanic> mechanicType) {
        super(mechanicType);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onVehicleEntityCollision(VehicleEntityCollisionEvent event) {
        if (!EventUtil.passesFilter(event)) {
            return;
        }

        if (!(event.getVehicle() instanceof Minecart)) {
            return;
        }

        if (!removeOtherCarts && (event.getEntity() instanceof Minecart)) {
            return;
        }

        if (!damagePlayers && event.getEntity() instanceof Player) {
            return;
        }

        if (!emptyCartsImpact && event.getVehicle() instanceof RideableMinecart && event.getVehicle().isEmpty()) {
            return;
        }

        if (event.getEntity() instanceof LivingEntity living) {
            if (living.isInsideVehicle()) {
                return;
            }

            living.damage(5);

            try {
                living.setVelocity(event.getVehicle().getVelocity().normalize().multiply(1.2).add(HALF_BLOCK_UP));
            } catch (IllegalArgumentException e) {
                living.setVelocity(HALF_BLOCK_UP);
            }
        } else if (removeOtherCarts && event.getEntity() instanceof Minecart && event.getEntity().isEmpty()) {
            event.getEntity().remove();
        } else {
            return;
        }

        event.setCancelled(true);
    }
}
