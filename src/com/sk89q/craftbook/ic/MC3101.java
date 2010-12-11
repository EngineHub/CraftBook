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

package com.sk89q.craftbook.ic;

import com.sk89q.craftbook.*;

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
 * Line 3: ##|ONCE or ##|INF -- where ## is the counter reset value, and ONCE or INF
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
        return "COUNTER";
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

		// TODO: More strict validation
        if (id.length() == 0 || !id.contains("|")) {
            return "Specify counter configuration on line 3.";
        }

		if(! sign.getLine4().equals("") ) {
			return "Line 4 must be blank";
		}

        return null;
    }

    /**
     * Think.
     *
     * @param chip
     */
    public void think(ChipState chip) {
		try {
    		// Get IC config data from line 3 of sign
    		String line3 = chip.getText().getLine3();
    		String[] config = line3.split("\\|");
    		int resetVal = Integer.parseInt(config[0].trim());
    		boolean inf = config[1].equals("INF");
    
    		// Get current counter value from line 4 of sign
    		String line4 = chip.getText().getLine4();
    		if(line4.equals(""))
    			line4 = "0";
    		int curVal = Integer.parseInt(line4.trim());
    		int oldVal = curVal;
    
    		// If clock input triggered
    		if(chip.getIn(1).isTriggered() && chip.getIn(1).is()) {
    			if(curVal == 0) { // if we've gotten to 0, reset if infinite mode
    				if(inf)
    					curVal = resetVal;
    			}
    			else // decrement counter
    				curVal--;
    
    			// set output to high if we're at 0, otherwise low
    			if(curVal == 0)
    				chip.getOut(1).set(true);
    			else
    				chip.getOut(1).set(false);
    		}
    
    		// if reset intput triggered, reset counter value
    		if(chip.getIn(2).isTriggered() && chip.getIn(2).is()) {
    			curVal = resetVal;
    		}
    
    		// update counter value stored on sign if it's changed
    		if(curVal != oldVal)
    			chip.getText().setLine4(Integer.toString(curVal));
    
    		// Clear error if one is set
    		if(chip.getText().getLine1().equals("ERROR")) {
    			chip.getText().setLine1(getTitle());
    			return;
    		}
		} catch (Exception e) {
			// catch errors, and let the user know something went horribly horribly wrong
			chip.getText().setLine1("ERROR");
			return;
		}
		
		chip.getText().supressUpdate();
    }
}
