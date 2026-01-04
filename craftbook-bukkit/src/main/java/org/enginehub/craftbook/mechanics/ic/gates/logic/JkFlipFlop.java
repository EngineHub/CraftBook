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
import org.enginehub.craftbook.BukkitChangedSign;
import org.enginehub.craftbook.mechanics.ic.AbstractIC;
import org.enginehub.craftbook.mechanics.ic.AbstractICFactory;
import org.enginehub.craftbook.mechanics.ic.ChipState;
import org.enginehub.craftbook.mechanics.ic.IC;
import org.enginehub.craftbook.mechanics.ic.ICFactory;

/**
 * A JK flip flop. A JK Flip Flop is like a SR Latch (S = J, R = K), but if both J and K is high, it
 * toggles,
 * and it has a clock.
 *
 * @author sindreij
 */
public class JkFlipFlop extends AbstractIC {

    public JkFlipFlop(Server server, BukkitChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    @Override
    public String getTitle() {

        return "JK negative edge-triggered flip flop";
    }

    @Override
    public String getSignTitle() {

        return "JK EDGE FLIP FLOP";
    }

    @Override
    public void trigger(ChipState chip) {

        boolean j = chip.getInput(1); // Set
        boolean k = chip.getInput(2); // Reset
        if (chip.isTriggered(0) && !chip.getInput(0)) {
            if (j && k) {
                chip.setOutput(0, !chip.getOutput(0));
            } else if (j && !k) {
                chip.setOutput(0, true);
            } else if (k) {
                chip.setOutput(0, false);
            }
        }
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public String[] getPinDescription(ChipState state) {

            return new String[] {
                "Trigger IC",//Inputs
                "Set",
                "Reset",
                "Output = Same if J and K = 0, 1 if J = 1, 0 if K = 1, and continuously toggling states if J and K = 1",//Outputs
            };
        }

        @Override
        public IC create(BukkitChangedSign sign) {

            return new JkFlipFlop(getServer(), sign, this);
        }
    }
}
