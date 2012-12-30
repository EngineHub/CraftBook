// $Id$
/*
 * Copyright (C) 2010, 2011 sk89q <http://www.sk89q.com>
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
  * warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not,
 * see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.craftbook.circuits.ic;

import com.sk89q.craftbook.BaseConfiguration;
import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.LocalPlayer;

/**
 * Factories are used to generate instances of ICs.
 *
 * @author sk89q
 */
public interface ICFactory {

    /**
     * Create an IC instance given a block. The verify method should already have been called before this function,
     * so this should have no reason to
     * fail or return a null.
     *
     * @param sign
     *
     * @return an IC ready to be used
     */
    public IC create(ChangedSign sign);

    /**
     * Verify that the IC can be created in the area of the world defined by the given sign; throw exceptions if not.
     * This does NOT verify
     * permissions, since that is only done when placing blocks for a new IC, and this can be invoked many times in
     * the life of an IC.
     *
     * @param sign
     *
     * @throws ICVerificationException if the area of the world defined by the sign does not represent a valid setup
     *                                 for this type of IC.
     */
    public void verify(ChangedSign sign) throws ICVerificationException;

    /**
     * Check the player who creates the IC, used in the MessageSender IC, to make sure people without the right
     * permission can't message others or the
     * whole server.
     *
     * @param sign
     * @param player
     *
     * @throws ICVerificationException if the area of the world defined by the sign does not represent a valid setup
     *                                 for this type of IC.
     */
    public void checkPlayer(ChangedSign sign, LocalPlayer player) throws ICVerificationException;

    /**
     * Get a short description of the IC
     *
     * @return a short description.
     */
    public String getDescription();

    /**
     * Get line-by-line help.
     *
     * @return array of lines 3 and 4
     */
    public String[] getLineHelp();

    /**
     * Adds config to the IC.
     *
     * @param section
     */
    public void addConfiguration(BaseConfiguration.BaseConfigurationSection section);

    /**
     * Check if IC uses configuration.
     *
     * @return if IC uses configuration.
     */
    public boolean needsConfiguration();
}
