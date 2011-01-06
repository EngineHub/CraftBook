package com.sk89q.craftbook.ic.types;
// $Id$
/*
 * CraftBook
 * Copyright (C) 2010 kutagh
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
 * @author kutagh
 * @author tmhrtly
 */
public class MC6000 extends BaseIC {
    /**
     * Get the title of the IC.
     *
     * @return
     */
    public String getTitle() {
        return "MONOSTABLE";
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
        
        sign.setLine3("OUTPUT: 0");
        sign.setLine4("0:0");
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
            String[] text = chip.getText().getLine4().split(":");
            boolean output = text[0].equals("1");
            boolean delayed = text[1].equals("0");
            boolean input = chip.getText().getLine3().equals("OUTPUT: 1");
            if(output && delayed)
            {
                chip.getOut(1).set(false);
                chip.getText().setLine4("0:5");
            }
            else if(output && !delayed)
            {
                int remaining = Integer.parseInt(text[1]) - 1;
                chip.getText().setLine4("1:" + Integer.toString(remaining));
            }
            else
            {
                if(input != chip.getIn(1).is())
                {
                    chip.getOut(1).set(true);
                    chip.getText().setLine3(chip.getIn(1).text());
                    chip.getText().setLine4("1:5");
                }
            }
            chip.getText().supressUpdate();
        } catch (Exception e) {
            chip.triggerError();
            
        }
    }
}
