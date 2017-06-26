/*
 * CraftBook Copyright (C) 2010-2017 sk89q <http://www.sk89q.com>
 * CraftBook Copyright (C) 2011-2017 me4502 <http://www.me4502.com>
 * CraftBook Copyright (C) Contributors
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
package com.sk89q.craftbook.sponge.mechanics.ics.chips.logic;

import com.sk89q.craftbook.sponge.mechanics.ics.IC;
import com.sk89q.craftbook.sponge.mechanics.ics.factory.ICFactory;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

abstract class AnyInputLogicGate extends IC {

    AnyInputLogicGate(ICFactory<? extends AnyInputLogicGate> icFactory, Location<World> block) {
        super(icFactory, block);
    }

    @Override
    public void trigger() {
        short on = 0, valid = 0;
        for (short i = 0; i < getPinSet().getInputCount(); i++) {
            if (getPinSet().isValid(i, this)) {
                valid++;

                if (getPinSet().getInput(i, this)) {
                    on++;
                }
            }
        }

        // Condition; all valid must be ON, at least one valid.
        getPinSet().setOutput(0, getResult(valid, on), this);
    }

    public abstract boolean getResult(int wires, int on);
}
