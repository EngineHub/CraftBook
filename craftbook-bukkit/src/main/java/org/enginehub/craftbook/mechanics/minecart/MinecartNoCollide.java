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
import org.bukkit.entity.Minecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;
import org.enginehub.craftbook.AbstractCraftBookMechanic;
import org.enginehub.craftbook.mechanic.CraftBookMechanic;
import org.enginehub.craftbook.mechanic.MechanicType;
import org.enginehub.craftbook.util.EventUtil;

public class MinecartNoCollide extends AbstractCraftBookMechanic {

    public MinecartNoCollide(MechanicType<? extends CraftBookMechanic> mechanicType) {
        super(mechanicType);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onCartCollision(VehicleEntityCollisionEvent event) {
        if (!EventUtil.passesFilter(event)) {
            return;
        }

        if (event.getVehicle() instanceof Minecart) {
            boolean isEmpty = event.getVehicle().isEmpty();
            if (isEmpty && !emptyCarts) {
                return;
            }
            if (!isEmpty && !fullCarts) {
                return;
            }

            event.setCancelled(true);
        }
    }

    private boolean emptyCarts;
    private boolean fullCarts;

    @Override
    public void loadFromConfiguration(YAMLProcessor config) {
        config.setComment("empty-carts", "Enable No Collide for empty carts.");
        emptyCarts = config.getBoolean("empty-carts", true);

        config.setComment("full-carts", "Enable No Collide for occupied carts.");
        fullCarts = config.getBoolean("full-carts", false);
    }
}
