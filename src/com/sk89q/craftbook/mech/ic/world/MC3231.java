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

import com.sk89q.craftbook.mech.ic.*;

/**
 * Time control.
 *
 * @author sk89q
 */
public class MC3231 extends BaseIC {
    /**
     * Get the title of the IC.
     *
     * @return
     */
    public String getTitle() {
        return "TIME CONTROL";
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
     * Think.
     *
     * @param chip
     */
    public void think(ChipState chip) {
        Long specific;

        if (chip.getIn(1).isTriggered() && chip.getIn(1).is()) {
            if (chip.getIn(2).is())
                specific = 0l;
            else
                specific = 13000l;

            chip.getWorld().setTime(specific);
        }
    }
}
