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

package com.sk89q.craftbook;

import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * A Mechanic is a an object that manages a set of BlockVectors to enhance those
 * positions with CraftBook functionality.
 * 
 * <p>
 * Mechanic instances are subject to lazy instantiation and must be able to
 * derive all of their internal state from the blocks in the world at any time.
 * Mechanic instances should be able to be discarded at essentially any time and
 * without warning, and yet be able to provide correct service whenever a new
 * Mechanic instance is created over the same BlockVector unless the contents of
 * the BlockVector have been otherwise interfered with in the intervening time
 * -- this is so that Mechanic instances can be discarded when their containing
 * chunks are unloaded, as well as for general insurance against server crashes.
 * </p>
 * 
 * @author sk89q
 * @author hash
 */
public interface Mechanic {

    /**
     * Unload this mechanic. This should free any allocated resources and
     * de-initialize. This may be called once the mechanic no longer exists
     * in the world.
     */
    public void unload();


    /**
     * @return true if this mechanic is still active in the world; false
     *         otherwise. For example, for a gate, it should check to see if the
     *         [Gate] sign still exists at the trigger points.
     */
    public boolean isActive();

    /**
     * Raised when a block is broken.
     * 
     * @param event
     */
    public void onBlockBreak(BlockBreakEvent event);

    /**
     * Raised when a block is right clicked.
     * 
     * @param event
     */
    public void onRightClick(PlayerInteractEvent event);


    /**
     * Raised when block is left clicked.
     * 
     * @param event
     */
    public void onLeftClick(PlayerInteractEvent event);


    /**
     * Raised when an input redstone current changes.
     * 
     * @param event
     */
    public void onBlockRedstoneChange(SourcedBlockRedstoneEvent event);

}