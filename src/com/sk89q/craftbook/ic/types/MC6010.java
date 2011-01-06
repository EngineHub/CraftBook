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
 * Output: HIGH when input has just changed state, LOW otherwise
 *
 * @author tmhrtly
 */
public class MC6010 extends BaseIC {
    /**
     * Get the title of the IC.
     *
     * @return
     */
    public String getTitle() {
        return "DELAY";
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
        if (sign.getLine3().length() == 0) {
            return "Specify a clock number on the third line.";
        }
        
        int clockTime;
        try {
            clockTime = Integer.parseInt(sign.getLine3());
        } catch (NumberFormatException e) {
            return "Clock rate is not a number.";
        }
        
        if (clockTime < 2){
            return "Clock rate must be a minimum of 2.";
        }
        if (clockTime > 15){
            return "Clock rate may not be greater than 15.";
        }

        if (sign.getLine4().length() != 0) {
            return "The fourth line must be empty.";
        }
        String x = "";
        for (int i = 0; i<clockTime; i++) {
            x += "-";
        } 
        sign.setLine4(x);
		sign.supressUpdate();
        return null;
    }

    /**
     * Think.
     *
     * @param chip
     */
    public void think(ChipState chip) {
        //try {
            String line = chip.getText().getLine4();
            boolean out = (line.charAt((line.length()-1)) == '+');
            chip.getOut(1).set(out);
            boolean in = chip.getIn(1).is();

            String x;
            if (in) {
                x = "+";
            } else {
                x = "-";
            }
            chip.getText().setLine4(x+line.substring(0,(line.length()-1)));
            chip.getText().supressUpdate();
        /*} catch (Exception e) {
            chip.triggerError();
        }*/
    }
}
