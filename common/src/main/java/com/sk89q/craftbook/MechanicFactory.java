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

package com.sk89q.craftbook;

import com.sk89q.worldedit.BlockWorldVector;

import org.bukkit.block.Sign;

/**
 * MechanicFactory attempts to detect a mechanism at a position and will produce
 * the corresponding Mechanic if it matches.
 * 
 * @author sk89q
 * @param <T>
 *            returned mechanic
 */
public interface MechanicFactory<T extends Mechanic> {
    /**
     * Detect the mechanic at a location.
     * 
     * @param pos
     * @return a {@link AbstractMechanic} if a mechanism could be found at the location;
     *         null otherwise
     * @throws InvalidMechanismException
     *             if it appears that the position is intended to me a
     *             mechanism, but the mechanism is misconfigured and inoperable.
     */
    public T detect(BlockWorldVector pos) throws InvalidMechanismException;
    
    /**
     * Detect the mechanic at a placed sign.
     * 
     * @param pos
     * @param player
     * @param sign
     * @return a {@link Mechanic} if a mechanism could be found at the location;
     *         null otherwise
     * @throws InvalidMechanismException
     *             if it appears that the position is intended to me a
     *             mechanism, but the mechanism is misconfigured and inoperable.
     * @throws ProcessedMechanismException
     */
    public T detect(BlockWorldVector pos, LocalPlayer player, Sign sign)
            throws InvalidMechanismException, ProcessedMechanismException;
    
}
