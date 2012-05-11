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

package com.sk89q.craftbook.ic.mechanic;

import com.sk89q.craftbook.ic.core.ICFactory;
import com.sk89q.craftbook.ic.core.ICFamily;

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
    protected Map<String, RegisteredICFactory> registered
            = new HashMap<String, RegisteredICFactory>();
    
    /**
     * Register an IC with the manager. The casing of the ID can be of any
     * case because IC IDs are case-insensitive. Re-using an already
     * registered name will override the previous registration.
     * 
     * @param id case-insensitive ID (such as MC1001)
     * @param factory factory to create ICs
     * @param nativeFamily the native family for the IC
     */
    public void register(String id, ICFactory factory, ICFamily nativeFamily) {
        RegisteredICFactory registration
                = new RegisteredICFactory(id, factory, nativeFamily);
        
        // Lowercase the ID so that we can do case in-sensitive lookups
        registered.put(id.toLowerCase(), registration);
    }
    
    /**
     * Get an IC registration by a provided ID.
     * 
     * @param id case insensitive ID
     * @return registration
     * @see RegisteredICFactory
     */
    public RegisteredICFactory get(String id) {
        return registered.get(id.toLowerCase());
    }
    
}
