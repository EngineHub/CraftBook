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
 * A JK flip flop.
 * A JK Flip Flop is like a SR Latch (S = J, R = K), but if both J and K is
 * high, it toggles, and it has a clock.
 *
 * @author sindreij
 */
public class MC3032 extends LogicIC {

    /**
     * Get the title of the IC.
     *
     * @return
     */
    public String getTitle() {

        return "JK EDGE FLIP FLOP";
    }

    /**
     * Think.
     *
     * @param chip
     */
    public void think(LogicChipState chip) {

        boolean j = chip.getIn(2).is(); //Set
        boolean k = chip.getIn(3).is(); //Reset
        if (chip.getIn(1).isTriggered() && chip.getIn(1).not()) {
            if (j && k) {
                chip.getOut(1).invert();
            } else if (j && !k) {
                chip.getOut(1).set(true);
            } else if (!j && k) {
                chip.getOut(1).set(false);
            }
        }
    }
}
