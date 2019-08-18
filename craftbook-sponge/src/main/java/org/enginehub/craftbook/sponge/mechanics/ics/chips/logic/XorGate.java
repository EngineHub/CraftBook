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

import org.enginehub.craftbook.sponge.mechanics.ics.factory.ICFactory;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class XorGate extends TwoInputLogicGate {

    public XorGate(ICFactory<XorGate> icFactory, Location<World> block) {
        super(icFactory, block);
    }

    @Override
    boolean getResult(boolean a, boolean b) {
        return a != b;
    }

    public static class Factory implements ICFactory<XorGate> {

        @Override
        public XorGate createInstance(Location<World> location) {
            return new XorGate(this, location);
        }

        @Override
        public String[][] getPinHelp() {
            return new String[][] {
                    new String[] {
                            "The XOR Operand"
                    },
                    new String[] {
                            "Outputs high if the inputs are not equal"
                    }
            };
        }
    }
}
