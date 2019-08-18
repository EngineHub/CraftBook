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
package org.enginehub.craftbook.sponge.mechanics.ics.chips.logic;

import org.enginehub.craftbook.sponge.mechanics.ics.IC;
import org.enginehub.craftbook.sponge.mechanics.ics.factory.ICFactory;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class LevelTriggerDFlipFlop extends IC {

    public LevelTriggerDFlipFlop(LevelTriggerDFlipFlop.Factory factory, Location<World> location) {
        super(factory, location);
    }

    @Override
    public void trigger() {
        if (getPinSet().getInput(0, this)) {
            getPinSet().setOutput(0, getPinSet().getInput(1, this), this);
        }
        if (getPinSet().getInput(2, this)) {
            getPinSet().setOutput(0, false, this);
        }
    }

    public static class Factory implements ICFactory<LevelTriggerDFlipFlop> {

        @Override
        public LevelTriggerDFlipFlop createInstance(Location<World> location) {
            return new LevelTriggerDFlipFlop(this, location);
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
                            "Trigger Value Carry",
                            "Value to Carry",
                            "Reset"
                    },
                    new String[] {
                            "Carried Value"
                    }
            };
        }
    }

}
