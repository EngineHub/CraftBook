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

/**
 * Integrated circuits are represented by this interface. For self-triggered
 * ICs, see {@link SelfTriggeredIC}.
 *
 * @author sk89q
 */
public interface IC {
    /**
     * @return the title of the IC.
     */
    public String getTitle();
    
    /**
     * @return the title that is shown on the sign.
     */
    public String getSignTitle();
    
    /**
     * Recieve a new state to process on.
     *
     * @param chip chip state.
     */
    public void trigger(ChipState chip);
    
    /**
     * Proceed to unload the IC.
     */
    public void unload();
}
