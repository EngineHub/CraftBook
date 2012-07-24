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
 * Broadcasts a message to nearby players.
 *
 * @author Tom (tmhrtly)
 */

import com.sk89q.craftbook.access.PlayerInterface;
import com.sk89q.craftbook.mech.ic.*;
import com.sk89q.craftbook.util.SignText;
import com.sk89q.craftbook.util.Vector;

public class MC1512 extends BaseIC {

    /**
     * Get the title of the IC.
     *
     * @return
     */
    public String getTitle() {

        return "MESSAGE NEARBY";
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

        String id = sign.getLine3();
        if (sign.getLine4().equals("")) {
            return "Please put a distance on the third line.";
        }
        if (id.length() == 0) {
            return "Please put a message on the 3rd line to be broadcast to nearby players.";
        }
        try {
            Double.parseDouble(sign.getLine4());
        } catch (NumberFormatException e) {
            return "Distance is not a number.";
        }

        return null;
    }

    public boolean requiresPermission() {

        return true; //Could be used for über-spam (with a large value on the 4th line, its practically like MC1511)
    }


    /**
     * Think.
     *
     * @param chip
     */
    public void think(ChipState chip) {

        if (chip.getIn(1).is()) { //Only on the rising edge
            String distance = chip.getText().getLine4();
            String theMessage = chip.getText().getLine3();
            Vector pos = chip.getBlockPosition();
            for (PlayerInterface p : chip.getWorld().getPlayerList())
                if (p.getPosition().distance(pos) <= (double) Float.parseFloat(distance))
                    p.sendMessage(theMessage);
        }
    }
}
