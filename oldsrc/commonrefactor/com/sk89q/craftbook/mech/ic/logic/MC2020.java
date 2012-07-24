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

import java.util.Random;

/**
 * 3-bit random number generator.
 *
 * @author sk89q
 */
public class MC2020 extends LogicIC {

    /**
     * Random number generator.
     */
    private static Random random = new Random();

    /**
     * Get the title of the IC.
     *
     * @return
     */
    public String getTitle() {

        return "3-BIT RANDOM";
    }

    /**
     * Think.
     *
     * @param chip
     */
    public void think(LogicChipState chip) {

        if (chip.getIn(1).is()) {
            chip.getOut(1).set(random.nextBoolean());
            chip.getOut(2).set(random.nextBoolean());
            chip.getOut(3).set(random.nextBoolean());
        }
    }
}
