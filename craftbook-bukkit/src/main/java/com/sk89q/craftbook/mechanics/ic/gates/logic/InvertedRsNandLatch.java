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

package com.sk89q.craftbook.mechanics.ic.gates.logic;

import org.bukkit.Server;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.mechanics.ic.AbstractIC;
import com.sk89q.craftbook.mechanics.ic.AbstractICFactory;
import com.sk89q.craftbook.mechanics.ic.ChipState;
import com.sk89q.craftbook.mechanics.ic.IC;
import com.sk89q.craftbook.mechanics.ic.ICFactory;

public class InvertedRsNandLatch extends AbstractIC {

    public InvertedRsNandLatch(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    @Override
    public String getTitle() {

        return "Inverted RS NAND latch";
    }

    @Override
    public String getSignTitle() {

        return "INV RS NAND LAT";
    }

    @Override
    public void trigger(ChipState chip) {

        boolean set = chip.getInput(0);
        boolean reset = chip.getInput(1);

        if (!set && !reset) {
            chip.setOutput(0, true);
        } else if (set && !reset) {
            chip.setOutput(0, false);
        } else if (!set && reset) {
            chip.setOutput(0, true);
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

            return new InvertedRsNandLatch(getServer(), sign, this);
        }
    }
}
