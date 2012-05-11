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

import org.bukkit.Server;
import org.bukkit.block.Sign;

/**
 * Integrated circuits templates are represented by this interface.
 *
 * @author Lymia
 */
public interface ICTemplate {
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
     * @param sign sign this was triggered on
     */
    public void trigger(ChipState chip, Sign sign);

    /**
     * Gets the server the template is attached to
     *
     * @return The server object
     */
    public Server getServer();

    /**
     * Verify that the IC can be created in the area of the world defined by the
     * given sign; throw exceptions if not.
     *
     * This does NOT verify permissions, since that is only done when placing
     * blocks for a new IC, and this can be invoked many times in the life of an
     * IC.
     *
     * @param sign The sign to verify.
     * @throws ICVerificationException
     *             if the area of the world defined by the sign does not
     *             represent a valid setup for this type of IC.
     */
    public void verify(Sign sign) throws ICVerificationException;
}
