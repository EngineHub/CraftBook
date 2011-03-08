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

import org.bukkit.World;
import org.bukkit.block.Block;

/**
 * IC families handle the logic required in figuring out where pins are
 * located and reading them. One ICFamily instance is created and attached
 * to the IC manager.
 * 
 * @author sk89q
 */
public interface ICFamily {
    
    /**
     * Return a {@link ChipState} that provides an interface to access
     * the I/O pins.
     * 
     * @param world
     * @param sign
     * @return
     */
    public ChipState detect(World world, Block sign);
    
}
