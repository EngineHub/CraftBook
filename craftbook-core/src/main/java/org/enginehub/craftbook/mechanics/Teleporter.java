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
 * Teleporter mechanic; teleports players to another location based on position.
 */
public abstract class Teleporter extends AbstractCraftBookMechanic {

    public Teleporter(MechanicType<? extends CraftBookMechanic> mechanicType) {
        super(mechanicType);
    }

    protected boolean requireSign;
    protected int maxRange;
    protected boolean buttonEnabled;

    @Override
    public void loadFromConfiguration(YAMLProcessor config) {
        config.setComment("require-sign", "Require a sign to be at the destination of the teleportation.");
        requireSign = config.getBoolean("require-sign", false);

        config.setComment("max-range", "The maximum distance between the start and end of a teleporter. Set to 0 for infinite.");
        maxRange = config.getInt("max-range", 0);

        config.setComment("enable-buttons", "Allow teleporters to be used by a button on the other side of the block.");
        buttonEnabled = config.getBoolean("enable-buttons", true);
    }
}
