package com.sk89q.craftbook.mech.ic.world;
// $Id$
/*
 * CraftBook
 * Copyright (C) 2010 tmhrtly <http://www.tmhrtly.com>
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

/**
 * Takes in a clock input, and outputs whether a specified player is online.
 *
 * @author Tom (tmhrtly)
 */

import com.sk89q.craftbook.access.ServerInterface;
import com.sk89q.craftbook.mech.ic.*;
import com.sk89q.craftbook.util.SignText;
import com.sk89q.craftbook.util.Vector;

public class MC1500 extends BaseIC {
    /**
     * Get the title of the IC.
     *
     * @return
     */
    public String getTitle() {
        return "IS PLAYER ONLINE";
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

        if (id.length() == 0 || id.contains(" ")) {
            return "Put a player's name on line 3, with no spaces.";
        }

        return null;
    }
    
    
    /**
     * Think.
     * 
     * @param chip
     */
    public void think(ChipState chip) {
        String thePlayer = chip.getText().getLine3();
        if (isPlayerOnline(chip.getServer(),thePlayer))
            chip.getOut(1).set(true);
        else 
            chip.getOut(1).set(false);
    }

    private boolean isPlayerOnline(ServerInterface s, String playerName) {
        return s.matchPlayer(playerName)!=null;
    }
}
