// $Id$
/*
 * Copyright (C) 2010, 2011 sk89q <http://www.sk89q.com>
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

public class EdgeTriggerDFlipFlop extends AbstractIC {

    public EdgeTriggerDFlipFlop(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    @Override
    public String getTitle() {

        return "Edge triggered D flip-flop";
    }

    @Override
    public String getSignTitle() {

        return "EDGE-D";
    }

    @Override
    public void trigger(ChipState chip) {

        if (chip.getInput(2)) {
            chip.setOutput(0, false);
        } else if (chip.getInput(1) && chip.isTriggered(1)) {
            chip.setOutput(0, chip.getInput(0));
        }

    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public String[] getPinDescription(ChipState state) {

            return new String[] {
                    "Carries over to Output 1",//Inputs
                    "Sets Output 1 to Input 1",
                    "Sets Output 1 to Low.",
                    "Carried Value"//Outputs
            };
        }

        @Override
        public IC create(ChangedSign sign) {

            return new EdgeTriggerDFlipFlop(getServer(), sign, this);
        }
    }
}
