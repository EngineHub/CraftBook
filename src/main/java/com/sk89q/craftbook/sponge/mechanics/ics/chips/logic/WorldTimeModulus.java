/*
 * CraftBook Copyright (C) 2010-2017 sk89q <http://www.sk89q.com>
 * CraftBook Copyright (C) 2011-2017 me4502 <http://www.me4502.com>
 * CraftBook Copyright (C) Contributors
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
package com.sk89q.craftbook.sponge.mechanics.ics.chips.logic;

import com.sk89q.craftbook.sponge.mechanics.ics.IC;
import com.sk89q.craftbook.sponge.mechanics.ics.factory.ICFactory;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class WorldTimeModulus extends IC {

    public WorldTimeModulus(ICFactory<WorldTimeModulus> icFactory, Location<World> block) {
        super(icFactory, block);
    }

    @Override
    public void trigger() {
        if (getPinSet().getInput(0, this)) {
            getPinSet().setOutput(0, getBlock().getExtent().getProperties().getWorldTime() % 2 == 1, this);
        }
    }

    public static class Factory implements ICFactory<WorldTimeModulus> {

        @Override
        public WorldTimeModulus createInstance(Location<World> location) {
            return new WorldTimeModulus(this, location);
        }

        @Override
        public String[][] getPinHelp() {
            return new String[][] {
                    new String[] {
                            "Input"
                    },
                    new String[] {
                            "Outputs high if world time is odd"
                    }
            };
        }
    }
}
