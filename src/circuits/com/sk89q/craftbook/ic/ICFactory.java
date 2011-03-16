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

import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

/**
 * Factories are used to generate instances of ICs.
 * 
 * @author sk89q
 */
public interface ICFactory {

    /**
     * Create an IC instance given a block. This should not fail and
     * return a null.
     * 
     * @param sign
     * @return
     */
    public IC create(Sign sign);
    
    /**
     * Verify that the IC can be created with the given sign. The sign will
     * be for the given IC factory.
     * 
     * @param sign
     * @param player
     * @throws ICVerificationException if there was an error
     */
    public void verify(Sign sign, Player player) throws ICVerificationException;
    
}
