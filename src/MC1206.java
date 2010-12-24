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

import com.sk89q.craftbook.*;
import com.sk89q.craftbook.ic.*;

/**
 * Dispenser.
 *
 * @author sk89q
 */
public class MC1206 extends BaseIC {
    /**
     * Get the title of the IC.
     *
     * @return
     */
    public String getTitle() {
        return "SET BLOCK BELOW";
    }

    /**
     * Returns true if this IC requires permission to use.
     *
     * @return
     */
    public boolean requiresPermission() {
        return true;
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
        String force = sign.getLine4();

        if (id.length() == 0) {
            return "Specify a block type on the third line.";
        } else if (getItem(id) == 0) {
            sign.setLine4("Force");
        } else if (getItem(id) < 1 && !force.equalsIgnoreCase("Force")) {
            return "Not a valid block type: " + sign.getLine3() + ".";
        }

        if (force.length() != 0 && !force.equalsIgnoreCase("Force")) {
            return "Fourth line needs to be blank or 'Force'.";
        }

        return null;
    }

    /**
     * Get an item from its name or ID.
     *
     * @param id
     * @return
     */
    private int getItem(String id) {
        try {
            return Integer.parseInt(id.trim());
        } catch (NumberFormatException e) {
            return etc.getDataSource().getItem(id.trim());
        }
    }

    /**
     * Think.
     *
     * @param chip
     */
    public void think(ChipState chip) {
        if (!chip.getIn(1).is()) {
            return;
        }

        String id = chip.getText().getLine3();
        String force = chip.getText().getLine4();
        boolean isForced = force.equalsIgnoreCase("Force");

        int item = getItem(id);

        if ((item > 0 || isForced) && !(item >= 21 && item <= 34)
                && item != 36) {
            Vector pos = chip.getBlockPosition();
            int y = pos.getBlockY() - 2;
            int x = pos.getBlockX();
            int z = pos.getBlockZ();

            if (y >= 0 && (isForced || CraftBook.getBlockID(x, y, z) == 0)) {
                CraftBook.setBlockID(x, y, z, item);
                chip.getOut(1).set(true);
            } else {
                chip.getOut(1).set(false);
            }

            return;
        }

        chip.getOut(1).set(false);
    }
}
