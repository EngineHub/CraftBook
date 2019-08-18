/*
 * CraftBook Copyright (C) 2010-2018 sk89q <http://www.sk89q.com>
 * CraftBook Copyright (C) 2011-2018 me4502 <http://www.me4502.com>
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

public class EdgeTriggerDFlipFlop extends IC {

    public EdgeTriggerDFlipFlop(EdgeTriggerDFlipFlop.Factory factory, Location<World> location) {
        super(factory, location);
    }

    @Override
    public void trigger() {
        if (getPinSet().getInput(2, this)) {
            getPinSet().setOutput(0, false, this);
        } else if (getPinSet().getInput(1, this) && getPinSet().isTriggered(1, this)) {
            getPinSet().setOutput(0, getPinSet().getInput(0, this), this);
        }
    }

    public static class Factory implements ICFactory<EdgeTriggerDFlipFlop> {

        @Override
        public EdgeTriggerDFlipFlop createInstance(Location<World> location) {
            return new EdgeTriggerDFlipFlop(this, location);
        }

        @Override
        public String[] getLineHelp() {
            return new String[] {
                    "",
                    ""
            };
        }

        @Override
        public String[][] getPinHelp() {
            return new String[][] {
                    new String[] {
                            "Value to Carry",
                            "Trigger Value Carry",
                            "Reset"
                    },
                    new String[] {
                            "Carried Value"
                    }
            };
        }
    }

}
