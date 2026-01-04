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
import org.enginehub.craftbook.bukkit.BukkitChangedSign;
import org.enginehub.craftbook.mechanics.ic.AbstractIC;
import org.enginehub.craftbook.mechanics.ic.AbstractICFactory;
import org.enginehub.craftbook.mechanics.ic.ChipState;
import org.enginehub.craftbook.mechanics.ic.IC;
import org.enginehub.craftbook.mechanics.ic.ICFactory;

public class Dispatcher extends AbstractIC {

    public Dispatcher(Server server, BukkitChangedSign block, ICFactory factory) {

        super(server, block, factory);
    }

    @Override
    public String getTitle() {

        return "Dispatcher";
    }

    @Override
    public String getSignTitle() {

        return "DISPATCHER";
    }

    @Override
    public void trigger(ChipState chip) {

        boolean value = chip.getInput(0);
        boolean targetB = chip.getInput(1);
        boolean targetC = chip.getInput(2);

        if (targetB) {
            chip.setOutput(1, value);
        }
        if (targetC) {
            chip.setOutput(2, value);
        }

    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(BukkitChangedSign sign) {

            return new Dispatcher(getServer(), sign, this);
        }

        @Override
        public String[] getPinDescription(ChipState state) {

            return new String[] {
                "Output Value",//Inputs
                "Output Left",
                "Output Right",
                "Nothing",//Outputs
                "Value if should output Left",
                "Value if should output Right"
            };
        }

        @Override
        public String getShortDescription() {

            return "Send middle signal out high sides.";
        }
    }
}
