// $Id$
/*
 * CraftBook
 * Copyright (C) 2010 Lymia <lymiahugs@gmail.com>
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
 * Multiplexer
 *
 * @author Lymia
 */
public class MC3040 extends LogicIC {

    /**
     * Get the title of the IC.
     *
     * @return
     */
    public String getTitle() {

        return "MULTIPLEXER";
    }

    /**
     * Think.
     *
     * @param chip
     */
    public void think(LogicChipState chip) {

        boolean swapper = chip.getIn(3).is();
        chip.getOut(1).set(swapper ? chip.getIn(1).is() : chip.getIn(2).is());
    }
}
