// $Id$
/*
 * CraftBook
 * Copyright (C) 2010 Sir Propane <http://github.com/teamrt-hank>
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
import com.sk89q.craftbook.util.SignText;
import com.sk89q.craftbook.util.Vector;

/**
 * Clock IC
 *
 * @author Sir Propane
 */
public class MC1420 extends LogicIC {

    /**
     * Get the title of the IC.
     *
     * @return
     */
    public String getTitle() {

        return "CLOCK";
    }

    /**
     * Validates the IC's environment. The position of the sign is given.
     * Return a string in order to state an error message and deny
     * creation, otherwise return null to allow.
     *
     * @param sign
     *
     * @return
     */
    public String validateEnvironment(Vector pos, SignText sign) {

        if (sign.getLine3().length() == 0) {
            return "Specify a clock number on the third line.";
        }

        int clockTime;
        try {
            clockTime = Integer.parseInt(sign.getLine3());
        } catch (NumberFormatException e) {
            return "Clock rate is not a number.";
        }

        if (clockTime < 5) {
            return "Clock rate must be a minimum of 5.";
        }
        if (clockTime > 15) {
            return "Clock rate may not be greater than 15.";
        }

        if (sign.getLine4().length() != 0) {
            return "The fourth line must be empty.";
        }

        return null;
    }

    /**
     * Think.
     *
     * @param chip
     */
    public void think(LogicChipState chip) {

        int clockTime = Integer.parseInt(chip.getText().getLine3());
        int count = chip.getText().getLine4().length();
        if (count % clockTime == clockTime - 1) {
            chip.getOut(1).set(!chip.getLast(1));
            chip.getText().setLine4("");
        } else chip.getText().setLine4(chip.getText().getLine4() + " ");

        chip.getText().supressUpdate();
    }
}