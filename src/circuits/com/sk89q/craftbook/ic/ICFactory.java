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
     * Create an IC instance given a block. The verify method should already
     * have been called before this function, so this should have no reason to
     * fail or return a null.
     * 
     * @param sign
     * @return an IC ready to be used
     */
    public IC create(Sign sign);

    /**
     * Verify that the IC can be created in the area of the world defined by the
     * given sign; throw exceptions if not.
     * 
     * This does NOT verify permissions, since that is only done when placing
     * blocks for a new IC, and this can be invoked many times in the life of an
     * IC.
     * 
     * @param sign
     * @param player
     *            who to report errors to. May be a no-op player if verification
     *            is taking place on an IC that is already built, but should
     *            never be null.
     * @throws ICVerificationException
     *             if the area of the world defined by the sign does not
     *             represent a valid setup for this type of IC.
     */
    public void verify(Sign sign, Player player) throws ICVerificationException;
    
    /**
     * @return the final segment of a permission string (i.e. "mc1200" for
     *         "craftbook.ic.restricted.mc1200"), or null if no permissions are
     *         required for creating this type of IC.
     */
    public String getPermissionName();
}
