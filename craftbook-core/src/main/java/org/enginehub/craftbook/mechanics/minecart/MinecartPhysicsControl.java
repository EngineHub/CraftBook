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
import org.enginehub.craftbook.AbstractCraftBookMechanic;
import org.enginehub.craftbook.mechanic.CraftBookMechanic;
import org.enginehub.craftbook.mechanic.MechanicType;

public abstract class MinecartPhysicsControl extends AbstractCraftBookMechanic {

    public MinecartPhysicsControl(MechanicType<? extends CraftBookMechanic> mechanicType) {
        super(mechanicType);
    }

    public boolean slowWhenEmpty;
    public double verticalFallSpeed;
    public double horizontalFallSpeed;
    public double maxSpeed;
    public double offRailSpeed;

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
