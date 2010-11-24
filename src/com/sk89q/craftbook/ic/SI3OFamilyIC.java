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

package com.sk89q.craftbook.ic;

import com.sk89q.craftbook.*;

/**
 * Single input, 3 output family of ICs.
 *
 * @author sk89q
 */
public abstract class SI3OFamilyIC {
    /**
     * Get a new state to use.
     *
     * @param pos
     * @param input1
     * @param oldState1
     * @param oldState2
     * @param oldState3
     * @param signText
     * @return 3 booleans or null
     */
    public abstract boolean[] think(Vector pos, boolean input1,
            boolean oldState1, boolean oldState2, boolean oldState3,
            SignText signText);
}
