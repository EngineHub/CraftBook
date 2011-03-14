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

import com.sk89q.craftbook.ic.families.*;

/**
 * Integrated circuits are represented by this interface. For self-triggered
 * ICs, see {@link SelfTriggeredIC}.
 *
 * @author sk89q
 */
public interface IC<CST extends ChipState, FT extends ICFamily<CST>> {
    /**
     * @return the title of the IC -- i.e. the human-readable name, not the IC
     *         ID string.
     */
    public String getTitle();
    
    /**
     * @return the ICFamily implementation that is used to define the
     *         translations between the world and the pins of the IC.
     */
    public FT getFamily();
    
    /**
     * Perform logic on the state; the state given contains the most recent view
     * of pins in the world, and any changes applied to the state will be
     * applied to the world after this method returns.
     * 
     * @param chip
     *            chip state.
     */
    public void trigger(CST chip);
    
    /**
     * Proceed to unload the IC.
     */
    public void unload();
    
}
