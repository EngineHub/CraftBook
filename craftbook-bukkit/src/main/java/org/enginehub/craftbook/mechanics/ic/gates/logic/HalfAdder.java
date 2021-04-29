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

package org.enginehub.craftbook.mechanics.ic.gates.logic;

import org.bukkit.Server;
import org.enginehub.craftbook.ChangedSign;
import org.enginehub.craftbook.mechanics.ic.AbstractIC;
import org.enginehub.craftbook.mechanics.ic.AbstractICFactory;
import org.enginehub.craftbook.mechanics.ic.ChipState;
import org.enginehub.craftbook.mechanics.ic.IC;
import org.enginehub.craftbook.mechanics.ic.ICFactory;

public class HalfAdder extends AbstractIC {

    public HalfAdder(Server server, ChangedSign block, ICFactory factory) {

        super(server, block, factory);
    }

    @Override
    public String getTitle() {

        return "Half Adder";
    }

    @Override
    public String getSignTitle() {

        return "HALF ADDER";
    }

    @Override
    public void trigger(ChipState chip) {

        boolean b = chip.getInput(1);
        boolean c = chip.getInput(2);

        boolean sum = b ^ c;
        boolean carry = b & c;

        chip.setOutput(0, sum);
        chip.setOutput(1, carry);
        chip.setOutput(2, carry);
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public String[] getPinDescription(ChipState state) {

            return new String[] {
                "Nothing",//Inputs
                "First bit to add",
                "Second bit to add",
                "Sum",//Outputs
                "Carry",
                "Carry (Same as Output 2)"
            };
        }

        @Override
        public IC create(ChangedSign sign) {

            return new HalfAdder(getServer(), sign, this);
        }
    }
}