// $Id$
/*
 * CraftBook
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.craftbook.mech.ic.logic;

import com.sk89q.craftbook.mech.ic.LogicChipState;
import com.sk89q.craftbook.mech.ic.LogicIC;

/**
 * D rising edge-triggered flip flop.
 *
 * @author sk89q
 */
public class MC3034 extends LogicIC {

    /**
     * Get the title of the IC.
     *
     * @return
     */
    public String getTitle() {

        return "D EDGE FLIPFLOP";
    }

    /**
     * Think.
     *
     * @param chip
     */
    public void think(LogicChipState chip) {

        if (chip.getIn(1).isTriggered() && chip.getIn(1).is()) {
            chip.getOut(1).set(chip.getIn(2).is());
        }

        if (chip.getIn(3).is()) {
            chip.getOut(1).set(false);
        }
    }
}
