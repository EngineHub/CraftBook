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
import org.bukkit.event.vehicle.VehicleCreateEvent;
import org.bukkit.util.Vector;
import org.enginehub.craftbook.AbstractCraftBookMechanic;
import org.enginehub.craftbook.mechanic.exception.MechanicInitializationException;
import org.enginehub.craftbook.util.EventUtil;

public class MinecartPhysicsControl extends AbstractCraftBookMechanic {
    private Vector fallSpeed;
    private Vector derailedVelocityMod;

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

    private boolean slowWhenEmpty;
    private double verticalFallSpeed;
    private double horizontalFallSpeed;
    private double maxSpeed;
    private double offRailSpeed;

    @Override
    public void loadFromConfiguration(YAMLProcessor config) {
        config.setComment("slow-when-empty", "Whether Minecarts should slow faster when empty");
        slowWhenEmpty = config.getBoolean("slow-when-empty", true);

        config.setComment("vertical-fall-speed", "Sets the vertical fall speed of the minecart");
        verticalFallSpeed = config.getDouble("vertical-fall-speed", -1);

        config.setComment("horizontal-fall-speed", "Sets the horizontal fall speed of the minecart");
        horizontalFallSpeed = config.getDouble("horizontal-fall-speed", -1);

        config.setComment("max-speed", "Sets the max speed modifier of carts. Normal Minecraft speed is 0.4");
        maxSpeed = config.getDouble("max-speed", -1);

        config.setComment("off-rail-speed", "Sets the off-rail speed modifier of carts");
        offRailSpeed = config.getDouble("off-rail-speed", -1);
    }
}
