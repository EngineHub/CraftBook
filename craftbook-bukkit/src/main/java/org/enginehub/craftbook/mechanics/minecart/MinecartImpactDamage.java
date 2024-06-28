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

package org.enginehub.craftbook.mechanics.minecart;

import com.sk89q.util.yaml.YAMLProcessor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.RideableMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;
import org.bukkit.util.Vector;
import org.enginehub.craftbook.AbstractCraftBookMechanic;
import org.enginehub.craftbook.mechanic.CraftBookMechanic;
import org.enginehub.craftbook.mechanic.MechanicType;
import org.enginehub.craftbook.util.EventUtil;

public class MinecartImpactDamage extends AbstractCraftBookMechanic {

    private static final Vector HALF_BLOCK_UP = new Vector(0, 0.5, 0);

    public MinecartImpactDamage(MechanicType<? extends CraftBookMechanic> mechanicType) {
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

    private boolean removeOtherCarts;
    private boolean emptyCartsImpact;
    private boolean damagePlayers;

    @Override
    public void loadFromConfiguration(YAMLProcessor config) {
        config.setComment("remove-other-minecarts", "Allow minecarts to remove other minecarts on impact.");
        removeOtherCarts = config.getBoolean("remove-other-minecarts", false);

        config.setComment("allow-empty-carts", "Allows the cart to be empty.");
        emptyCartsImpact = config.getBoolean("allow-empty-carts", false);

        config.setComment("damage-players", "Allows the cart to damage and kill players.");
        damagePlayers = config.getBoolean("damage-players", true);
    }
}
