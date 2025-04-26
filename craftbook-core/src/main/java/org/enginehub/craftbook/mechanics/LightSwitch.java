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

package org.enginehub.craftbook.mechanics;

import com.sk89q.util.yaml.YAMLProcessor;
import org.enginehub.craftbook.AbstractCraftBookMechanic;
import org.enginehub.craftbook.mechanic.CraftBookMechanic;
import org.enginehub.craftbook.mechanic.MechanicType;

/**
 * Handler for Light switches. Toggles all torches in the area from being redstone to normal
 * torches. This is done
 * every time a sign with [|] or [I]
 * is right clicked by a player.
 */
public abstract class LightSwitch extends AbstractCraftBookMechanic {

    public LightSwitch(MechanicType<? extends CraftBookMechanic> mechanicType) {
        super(mechanicType);
    }

    protected int maxRange;
    protected int maxLights;

    @Override
    public void loadFromConfiguration(YAMLProcessor config) {
        config.setComment("max-range", "The maximum range that the mechanic searches for lights in.");
        maxRange = config.getInt("max-range", 10);

        config.setComment("max-lights", "The maximum amount of lights that a light switch can toggle per usage.");
        maxLights = config.getInt("max-lights", 20);
    }
}
