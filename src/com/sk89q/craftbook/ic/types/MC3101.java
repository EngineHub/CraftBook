// $Id$
/*
 * CraftBook
 * Copyright (C) 2010 davr <http://blog.davr.org/>
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

package com.sk89q.craftbook.ic.types;

import com.sk89q.craftbook.ic.BaseIC;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.util.SignText;
import com.sk89q.craftbook.util.Vector;

/**
 * Counter IC
 *
 * 3ISO family chip
 *
 * Counter counts down each time clock input toggles from low to high, it starts 
 * from a predefined value to 0. Output is high when counter reaches 0. If in 
 * 'infinite' mode, it will automatically reset the next time clock is toggled. 
 * Otherwise, it only resets when the 'reset' input toggles from low to high.
 *
 * Configuration:
 * Line 3: ##:ONCE or ##:INF -- where ## is the counter reset value, and ONCE or INF
 *         specifies if the counter should repeat or not.
 * Line 4: 0 -- must be set to 0 (TODO: Make it auto set the counter to 0 on creation)
 *
 * Inputs:
 *  1 - Clock
 *  2 - Reset
 *  3 - (unused)
 *
 * Output: HIGH when counter reaches 0, LOW otherwise
 *
 * @author davr
 */
public class MC3101 extends BaseIC {
    /**
     * Get the title of the IC.
     *
     * @return
     */
    public String getTitle() {
        return "DOWN COUNTER";
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
        String id = sign.getLine3();

        if (id.length() == 0 || !id.matches("^[0-9]+:(INF|ONCE)$")) {
            return "Specify counter configuration on line 3.";
        }

        if (!sign.getLine4().equals("")) {
            return "Line 4 must be blank";
        }
        
        sign.setLine4("0");

        return null;
    }

    /**
     * Think.
     *
     * @param chip
     */
    public void think(ChipState chip) {
        try {
            // Get IC configuration data from line 3 of sign
            String line3 = chip.getText().getLine3();
            String[] config = line3.split(":");
            
            int resetVal = Integer.parseInt(config[0]);
            boolean inf = config[1].equals("INF");
    
            // Get current counter value from line 4 of sign
            String line4 = chip.getText().getLine4();
            int curVal = Integer.parseInt(line4);
            int oldVal = curVal;
    
            // If clock input triggered
            if (chip.getIn(1).isTriggered() && chip.getIn(1).is()) {
                if (curVal == 0) { // If we've gotten to 0, reset if infinite mode
                    if (inf) {
                        curVal = resetVal;
                    }
                } else { // Decrement counter
                    curVal--;
                }
    
                // Set output to high if we're at 0, otherwise low
                if (curVal == 0) {
                    chip.getOut(1).set(true);
                } else {
                    chip.getOut(1).set(false);
                }
            // If reset input triggered, reset counter value
            } else if (chip.getIn(2).isTriggered() && chip.getIn(2).is()) {
                curVal = resetVal;
            }
    
            // Update counter value stored on sign if it's changed
            if (curVal != oldVal) {
                chip.getText().setLine4(Integer.toString(curVal));
            }
            
            chip.getText().supressUpdate();
        } catch (Exception e) {
            chip.triggerError();
        }
    }
}
