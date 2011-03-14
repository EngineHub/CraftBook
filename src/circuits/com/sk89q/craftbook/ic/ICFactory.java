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

import org.bukkit.block.Block;

import com.sk89q.craftbook.ic.families.*;

/**
 * Factories are used to generate instances of ICs.
 * 
 * @author sk89q
 */
public interface ICFactory<ICT extends IC<CST, FT>, CST extends ChipState, FT extends ICFamily<CST>> {
    /**
     * @param center the Block that contains the sign that defines the IC
     * @return a new IC
     */
    public ICT create(Block center);
    
    /**
     * ICFactory instances are registered in ICMechanicFactory using this
     * string, and as such it determines when this factory is called.
     * 
     * @return the MC ID string as it would appear on a sign (i.e. "[MC1000]").
     */
    public String getID();
}
