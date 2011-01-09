package com.sk89q.craftbook.ic.types;
// $Id$
/*
 * CraftBook
 * Copyright (C) 2010 tmhrtly
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


import com.sk89q.craftbook.ic.*;
import com.sk89q.craftbook.util.SignText;
import com.sk89q.craftbook.util.Vector;

/**
 * Monostable
 *
 * SSTS family chip
 *
 * Inputs:
 *  1 - Clock
 *
 * Output: HIGH when input has just changed state to high, LOW otherwise
 *
 * @author tmhrtly
 */
public class MC6001 extends BaseIC {
    /**
     * Get the title of the IC.
     *
     * @return
     */
    public String getTitle() {
        return "MONOSTABLE-ON";
    }

    /**
     * Validates the IC's environment. The position of the sign is given.
     * Return a string in order to state an error message and deny
     * creation, otherwise return null to allow.
     *
     * @param sign
     * @return
     */
    public String validateEnvironment(Vector pos, SignText sign) {
        if (!sign.getLine3().equals("")) {
            return "Line 3 must be blank";
        }

        if (!sign.getLine4().equals("")) {
            return "Line 4 must be blank";
        }
        
        sign.setLine3("0");
        sign.supressUpdate();

        return null;
    }

    /**
     * Think.
     *
     * @param chip
     */
    public void think(ChipState chip) {
        try {
            int ticksLeft = Integer.parseInt(chip.getText().getLine3());
            if (ticksLeft>1 ) {
                chip.getOut(1).set(true);
                chip.getText().setLine3(Integer.toString(ticksLeft-1));
                chip.getText().supressUpdate();
            } else if (chip.getIn(1).is() && ticksLeft < 1) {
                chip.getText().setLine3("6");
                chip.getText().supressUpdate();
            } else if (ticksLeft == 1 && chip.getIn(1).not()){
                chip.getOut(1).set(false);
                chip.getText().setLine3("0");
                chip.getText().supressUpdate();
            } else if (ticksLeft == 1 && chip.getIn(1).not()) {
                chip.getOut(1).set(false);
            } else {
                chip.getOut(1).set(false);
            }
        } catch (Exception e) {
            chip.triggerError();
            
        }
    }
}
