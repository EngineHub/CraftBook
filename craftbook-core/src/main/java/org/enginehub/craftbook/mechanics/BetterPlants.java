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

public abstract class BetterPlants extends AbstractCraftBookMechanic {

    public BetterPlants(MechanicType<? extends CraftBookMechanic> mechanicType) {
        super(mechanicType);
    }

    protected boolean fernFarming;
    protected boolean fastTickRandoms;

    @Override
    public void loadFromConfiguration(YAMLProcessor config) {
        config.setComment("fern-farming", "Allows ferns to be farmed by breaking top half of a large fern. (And small ferns to grow)");
        fernFarming = config.getBoolean("fern-farming", true);

        config.setComment("fast-random-ticks", "Use a way of generating less random numbers, by only generating it once for all chunks, instead of one each chunk.");
        fastTickRandoms = config.getBoolean("fast-random-ticks", true);
    }
}
