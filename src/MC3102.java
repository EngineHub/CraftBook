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
 * Line 3: time
 *
 * Inputs:
 *  1 - Clock
 *  2 - Start
 *  3 - Start
 *
 * Output: HIGH PULSE when counter reaches 0, LOW otherwise
 *
 *
 * @author protito based on MC3101 by davr
 */
public class MC3102 extends BaseIC {

    /**
     * Get the title of the IC.
     *
     * @return
     */
    public String getTitle() {
        return "DELAY BY";
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
        String delayVal = sign.getLine3();

		// TODO: More strict validation
        if (delayVal.length() == 0 || Integer.parseInt(delayVal)<0) {
            return "Specify Delay time in line 3.";
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
    		int delayVal = Integer.parseInt(line3.trim());
    		
    		// Get current delay value from line 4 of sign
    		String line4 = chip.getText().getLine4();
    		if(line4.equals(""))
    			line4 = Integer.toString(delayVal)+'|'+'0';
			String vars[] = line4.split("\\|");
    		int curVal = Integer.parseInt(vars[0].trim());
			if (curVal < 0)
				curVal = 0;
    		boolean active = vars[1].equals("1");
    
    		// If clock input triggered
    		if(chip.getIn(1).isTriggered() && chip.getIn(1).is()) {
    			if (active){
					curVal--;
					chip.getText().setLine4(Integer.toString(curVal)+'|'+'1');
					// set output to high if we're at 0
					if(curVal <= 0){
						chip.getOut(1).set(true);
						//set active false.. only one clock cycle will output HIGH
						chip.getText().setLine4(Integer.toString(delayVal)+'|'+'0');
						}
					else
						chip.getOut(1).set(false);
					}
				else{
				chip.getOut(1).set(false);
				}
    		}
			
    
    		// if start input triggered
    		if((chip.getIn(2).isTriggered() && chip.getIn(2).is()) 
				|| (chip.getIn(3).isTriggered() && chip.getIn(3).is())
				&& !active) {
    			curVal = delayVal;
				chip.getText().setLine4(Integer.toString(delayVal)+'|'+'1');
    		}
    
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
