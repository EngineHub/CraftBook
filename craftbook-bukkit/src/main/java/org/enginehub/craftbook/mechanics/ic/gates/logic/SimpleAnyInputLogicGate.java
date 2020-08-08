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
import org.enginehub.craftbook.mechanics.ic.ChipState;
import org.enginehub.craftbook.mechanics.ic.ICFactory;

public abstract class SimpleAnyInputLogicGate extends AbstractIC {

    public SimpleAnyInputLogicGate(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    @Override
    public void trigger(ChipState chip) {

        short on = 0, valid = 0;
        for (short i = 0; i < chip.getInputCount(); i++) {
            if (chip.isValid(i)) {
                valid++;

                if (chip.getInput(i)) {
                    on++;
                }
            }
        }

        // Condition; all valid must be ON, at least one valid.
        chip.setOutput(0, getResult(valid, on));
    }

    protected abstract boolean getResult(int wires, int on);
}
