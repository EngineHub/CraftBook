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

public class FullAdder extends IC {

    public FullAdder(FullAdder.Factory factory, Location<World> location) {
        super(factory, location);
    }

    @Override
    public void trigger() {
        boolean A = getPinSet().getInput(0, this);
        boolean B = getPinSet().getInput(1, this);
        boolean C = getPinSet().getInput(2, this);

        boolean S = A ^ B ^ C;
        boolean Ca = A & B | (A ^ B) & C;

        getPinSet().setOutput(0, S, this);
        getPinSet().setOutput(1, Ca, this);
        getPinSet().setOutput(2, Ca, this);
    }

    public static class Factory implements ICFactory<FullAdder> {

        @Override
        public FullAdder createInstance(Location<World> location) {
            return new FullAdder(this, location);
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
                            "First Operand",
                            "Second Operand",
                            "Carry Bit"
                    },
                    new String[] {
                            "Sum",
                            "Carry Out",
                            "Carry Out (Same as Output 2)"
                    }
            };
        }
    }
}
