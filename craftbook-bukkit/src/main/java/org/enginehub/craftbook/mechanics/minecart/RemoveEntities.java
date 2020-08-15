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
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.entity.minecart.RideableMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;
import org.bukkit.util.Vector;
import org.enginehub.craftbook.AbstractCraftBookMechanic;
import org.enginehub.craftbook.util.EventUtil;

public class RemoveEntities extends AbstractCraftBookMechanic {

    @EventHandler(priority = EventPriority.HIGH)
    public void onVehicleEntityCollision(VehicleEntityCollisionEvent event) {

        if (!EventUtil.passesFilter(event)) return;

        if (!(event.getVehicle() instanceof Minecart))
            return;

        if (!otherCarts && (event.getEntity() instanceof Minecart || event.getEntity().isInsideVehicle()))
            return;

        if (!players && event.getEntity() instanceof Player) {
            return;
        }

        if (event.getVehicle() instanceof RideableMinecart && event.getVehicle().isEmpty() && !empty)
            return;

        if (event.getEntity() instanceof LivingEntity) {
            if (event.getEntity().isInsideVehicle())
                return;
            ((LivingEntity) event.getEntity()).damage(10);
            Vector newVelocity = event.getVehicle().getVelocity().normalize().multiply(1.8).add(new Vector(0, 0.5, 0));
            if (Double.isFinite(newVelocity.getX()) && Double.isFinite(newVelocity.getY()) && Double.isFinite(newVelocity.getZ())) {
                event.getEntity().setVelocity(newVelocity);
            }
        } else if (event.getEntity() instanceof Vehicle) {

            if (!event.getEntity().isEmpty())
                return;
            else
                event.getEntity().remove();
        } else
            event.getEntity().remove();

        event.setCancelled(true);
        event.setPickupCancelled(true);
        event.setCollisionCancelled(true);
    }

    private boolean otherCarts;
    private boolean empty;
    private boolean players;

    @Override
    public void loadFromConfiguration(YAMLProcessor config) {

        config.setComment("remove-other-minecarts", "Allows the remove entities mechanic to remove other minecarts.");
        otherCarts = config.getBoolean("remove-other-minecarts", false);

        config.setComment("allow-empty-carts", "Allows the cart to be empty.");
        empty = config.getBoolean("allow-empty-carts", false);

        config.setComment("damage-players", "Allows the cart to damage and kill players.");
        players = config.getBoolean("damage-players", true);
    }
}