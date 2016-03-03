/*
 * CraftBook Copyright (C) 2010-2016 sk89q <http://www.sk89q.com>
 * CraftBook Copyright (C) 2011-2016 me4502 <http://www.me4502.com>
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
import com.sk89q.craftbook.sponge.mechanics.ics.ICType;
import org.spongepowered.api.world.Location;

public abstract class TwoInputLogicGate extends IC {

    public TwoInputLogicGate(ICType<? extends IC> type, Location block) {
        super(type, block);
    }

    @Override
    public void trigger() {

        Boolean a = null;
        Boolean b = null;

        // New input handling: any/first two valid inputs discovered. Moar flexibility!
        for (int i = 0; i < getPinSet().getInputCount(); i++) {
            if (getPinSet().isValid(i, this)) {
                boolean pinval = getPinSet().getInput(i, this);
                // Got pin value, assign to first free variable, break if got both.
                if (a == null) {
                    a = pinval;
                } else if (b == null) {
                    b = pinval;
                } else {
                    break;
                }
            }
        }

        if (a == null || b == null) return;

        getPinSet().setOutput(0, getResult(a, b), this);
    }

    protected abstract boolean getResult(boolean a, boolean b);
}
