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

public class DeMultiplexer extends AbstractIC {

    public DeMultiplexer(Server server, BukkitChangedSign sign, ICFactory factory) {
        super(server, sign, factory);
    }

    @Override
    public String getTitle() {

        return "De-Multiplexer";
    }

    @Override
    public String getSignTitle() {

        return "DEMULTIPLEXER";
    }

    @Override
    public void trigger(ChipState chip) {

        boolean swapper = chip.getInput(0);
        chip.setOutput(swapper ? 1 : 2, chip.getInput(1));
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public String[] getPinDescription(ChipState state) {

            return new String[] {
                "Swaps between Output 2 and 3",//Inputs
                "Value to carry",
                "Nothing",
                "Nothing",//Outputs
                "Output if Input 1 Low",
                "Output if Input 1 High"
            };
        }

        @Override
        public IC create(BukkitChangedSign sign) {

            return new DeMultiplexer(getServer(), sign, this);
        }
    }
}