// $Id$
/*
 * Copyright (C) 2010, 2011 sk89q <http://www.sk89q.com>
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

/**
 * Shortcut methods for a triple input IC. The included static methods allow
 * mapping inputs and outputs to a ChipState. The first three inputs are
 * 0, 1, and 2 while the outputs are 4 and beyond.
 * 
 * @author sk89q
 */
public class TripleInputChipState {
    
    private TripleInputChipState() {
        // Can't construct this
    }

    public static boolean input(ChipState chipState, int num) {
        return chipState.get(num);
    }

    public static boolean getOutput(ChipState chipState, int num) {
        return chipState.get(3 + num);
    }

    public static void output(ChipState chipState, int num, boolean val) {
        chipState.set(3 + num, val);
    }
    
}
