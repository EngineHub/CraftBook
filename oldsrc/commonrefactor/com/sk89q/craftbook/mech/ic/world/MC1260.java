package com.sk89q.craftbook.mech.ic.world;
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

import com.sk89q.craftbook.mech.ic.BaseIC;
import com.sk89q.craftbook.mech.ic.ChipState;
import com.sk89q.craftbook.util.SignText;
import com.sk89q.craftbook.util.Vector;

/**
 * Checks for the presence of water.
 *
 * @author yofreke
 */
public class MC1260 extends BaseIC {
    /**
     * Trigger only on rising edge.
     */
    private boolean triggerOnRising = false;
    
    /**
     * Construct the object.
     * 
     * @param triggerOnRising
     */
    public MC1260(boolean triggerOnRising) {
        this.triggerOnRising = triggerOnRising;
    }
    
    /**
     * Get the title of the IC.
     *
     * @return
     */
    public String getTitle() {
        return "WATER SENSOR";
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
        String yOffsetLine = sign.getLine3();

        try {
            if (yOffsetLine.length() > 0) {
                Integer.parseInt(yOffsetLine);
            }
        } catch (NumberFormatException e) {
            return "The third line must be a number or be blank.";
        }

        return null;
    }

    /**
     * Think.
     *
     * @param chip
     */
    public void think(ChipState chip){
        if (triggerOnRising && !chip.getIn(1).is()) {
            return;
        }
        
        Vector blockPos = chip.getBlockPosition();
        
        int x = blockPos.getBlockX();
        int y = blockPos.getBlockY();
        int z = blockPos.getBlockZ();
        
        try{
            String yOffsetLine = chip.getText().getLine3();
            
            if (yOffsetLine.length() > 0) {
                y += Integer.parseInt(yOffsetLine);
            } else {
                y -= 1;
            }
        } catch (NumberFormatException e) {
            y -= 1;
        }
        
        y = Math.min(Math.max(0, y), 127);
        
        int type = chip.getWorld().getId(x, y, z);
        
        chip.getOut(1).set(type == 8 || type == 9);
    }
}

