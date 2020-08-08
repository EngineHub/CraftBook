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

package org.enginehub.craftbook.mechanics.ic.gates.logic;

import org.bukkit.Server;

import org.enginehub.craftbook.ChangedSign;
import org.enginehub.craftbook.mechanics.ic.AbstractIC;
import org.enginehub.craftbook.mechanics.ic.AbstractICFactory;
import org.enginehub.craftbook.mechanics.ic.ChipState;
import org.enginehub.craftbook.mechanics.ic.IC;
import org.enginehub.craftbook.mechanics.ic.ICFactory;

/**
 * Simulates the function of a SR latch made from NAND gates.
 */
public class RsNandLatch extends AbstractIC {

    public RsNandLatch(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    @Override
    public String getTitle() {

        return "RS NAND latch";
    }

    @Override
    public String getSignTitle() {

        return "RS NAND LATCH";
    }

    @Override
    public void trigger(ChipState chip) {

        boolean set = !chip.getInput(0);
        boolean reset = !chip.getInput(1);
        if (!set && !reset) {
            chip.setOutput(0, true);
        } else if (!set && reset) {
            chip.setOutput(0, true);
        } else if (!reset) {
            chip.setOutput(0, false);
        }
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public String[] getPinDescription(ChipState state) {

            return new String[] {
                    "Set",//Inputs
                    "Reset",
                    "Nothing",
                    "Output",//Outputs
            };
        }

        @Override
        public IC create(ChangedSign sign) {

            return new RsNandLatch(getServer(), sign, this);
        }
    }
}
