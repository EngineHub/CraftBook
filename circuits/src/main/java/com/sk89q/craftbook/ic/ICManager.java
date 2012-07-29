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

import com.sk89q.worldedit.BlockWorldVector;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages known registered ICs. For an IC to be detected in-world through
 * CraftBook, the IC's factory has to be registered with this manager.
 *
 * @author sk89q
 */
public class ICManager {

    /**
     * Holds a map of registered IC factories with their ID.
     *
     * @see RegisteredICFactory
     */
    protected final Map<String, RegisteredICFactory> registered
            = new HashMap<String, RegisteredICFactory>();

    private static final Map<BlockWorldVector, IC> cachedICs
            = new HashMap<BlockWorldVector, IC>();

    /**
     * Register an IC with the manager. The casing of the ID can be of any
     * case because IC IDs are case-insensitive. Re-using an already
     * registered name will override the previous registration.
     *
     * @param id           case-insensitive ID (such as MC1001)
     * @param factory      factory to create ICs
     * @param families families for the ic
     */
    public void register(String id, ICFactory factory, ICFamily... families) {

	    for (ICFamily family : families) {
		    id = id.replace("MC", family.getModifier());
		    RegisteredICFactory registration
				    = new RegisteredICFactory(id, factory, family);
		    // Lowercase the ID so that we can do case in-sensitive lookups
		    registered.put(id.toLowerCase(), registration);
	    }

    }

    /**
     * Get an IC registration by a provided ID.
     *
     * @param id case insensitive ID
     *
     * @return registration
     *
     * @see RegisteredICFactory
     */
    public RegisteredICFactory get(String id) {

        return registered.get(id.toLowerCase());
    }

    /**
     * Checks if the IC Mechanic at the given point is
     * cached. If not it will return false.
     *
     * @param pt of the ic
     *
     * @return true if ic is cached
     */
    public static boolean isCachedIC(BlockWorldVector pt) {

        return cachedICs.containsKey(pt);
    }

    /**
     * Gets the cached IC based on its location in the world.
     * isCached should be checked before calling this method.
     *
     * @param pt of the ic
     *
     * @return cached ic.
     */
    public static IC getCachedIC(BlockWorldVector pt) {

        return cachedICs.get(pt);
    }

    /**
     * Adds the given IC to the cached IC list.
     *
     * @param pt of the ic
     * @param ic to add
     */
    public static void addCachedIC(BlockWorldVector pt, IC ic) {

        cachedICs.put(pt, ic);
    }

    /**
     * Removes the given IC from the cache list based
     * on its location.
     *
     * @param pt of the ic
     *
     * @return the removed ic
     */
    public static IC removeCachedIC(BlockWorldVector pt) {

        if (cachedICs.containsKey(pt)) {
            return cachedICs.remove(pt);
        }
        return null;
    }

    /**
     * Gets called when the IC gets unloaded.
     * This method then takes care of clearing the IC
     * from the cache.
     *
     * @param pt of the block break
     */
    public static void unloadIC(BlockWorldVector pt) {

        removeCachedIC(pt);
    }
}
