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

package com.sk89q.craftbook.mechanics.ic;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.CraftBookPlayer;

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
    IC create(ChangedSign sign);

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
    void verify(ChangedSign sign) throws ICVerificationException;

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
    void checkPlayer(ChangedSign sign, CraftBookPlayer player) throws ICVerificationException;

    /**
     * Get a short description of the IC
     *
     * @return a short description.
     */
    String getShortDescription();

    /**
     * Get a long description, to be used for wiki generation.
     * 
     * This description must fit the guidelines of MediaWiki syntax,
     * as it is built to work on a wiki running MediaWiki.
     * 
     * @return an array containing each line of the long description of the IC's usage.
     */
    String[] getLongDescription();

    /**
     * Get line-by-line help.
     *
     * @return array of lines 3 and 4
     */
    String[] getLineHelp();

    /**
     * Get description of the function of each pin of the IC.
     * 
     * @param state The {@link ChipState} that the pins are attached to.
     * 
     * @return array of each pin in order.
     */
    String[] getPinDescription(ChipState state);

    /**
     * Called on load to load extra information.
     */
    void load();

    /**
     * Unloads the IC Factory.
     */
    void unload();
}
