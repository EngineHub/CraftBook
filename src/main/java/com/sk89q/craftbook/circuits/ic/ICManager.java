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

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

import org.bukkit.Location;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.util.RegexUtil;

/**
 * Manages known registered ICs. For an IC to be detected in-world through CraftBook,
 * the IC's factory has to be registered with this manager.
 *
 * @author sk89q
 */
public class ICManager {

    /**
     * Holds a map of registered IC factories with their ID.
     *
     * @see RegisteredICFactory
     */
    public final Map<String, RegisteredICFactory> registered = new HashMap<String, RegisteredICFactory>();

    /**
     * Holds a map of long IDs to short IDs
     *
     * @see RegisteredICFactory
     */
    public final Map<String, String> longRegistered = new HashMap<String, String>();

    private static final Map<Location, IC> cachedICs = new HashMap<Location, IC>();

    private static final Set<String> customPrefix = new HashSet<String>();

    /**
     * Register an IC with the manager. The casing of the ID can be of any case because IC IDs are case-insensitive.
     * Re-using an already registered
     * name will override the previous registration.
     *
     * @param id       case-insensitive ID (such as MC1001)
     * @param factory  factory to create ICs
     * @param families families for the ic
     */
    public void register(String id, ICFactory factory, ICFamily... families) {

        register(id, null, factory, families);
    }

    /**
     * Register an IC with the manager. The casing of the ID can be of any case because IC IDs are case-insensitive.
     * Re-using an already registered
     * name will override the previous registration.
     *
     * @param id       case-insensitive ID (such as MC1001)
     * @param longId   case-insensitive long name (such as inverter)
     * @param factory  factory to create ICs
     * @param families families for the ic
     *
     * @return true if IC registration was a success
     */
    public boolean register(String id, String longId, ICFactory factory, ICFamily... families) {

        // check if at least one family is given
        if (families.length < 1) return false;
        // this is needed so we dont have two patterns
        String id2 = "[" + id + "]";
        // lets check if the IC ID has already been registered
        if (registered.containsKey(id.toLowerCase(Locale.ENGLISH))) return false;
        // check if the ic matches the requirements
        Matcher matcher = RegexUtil.IC_PATTERN.matcher(id2);
        if (!matcher.matches()) return false;
        String prefix = matcher.group(2).toLowerCase(Locale.ENGLISH);
        // lets get the custom prefix
        customPrefix.add(prefix);

        RegisteredICFactory registration = new RegisteredICFactory(id, longId, factory, families);
        // Lowercase the ID so that we can do case in-sensitive lookups
        registered.put(id.toLowerCase(Locale.ENGLISH), registration);

        if (longId != null) {
            String toRegister = longId.toLowerCase(Locale.ENGLISH);
            if (toRegister.length() > 15) {
                toRegister = toRegister.substring(0, 15);
            }
            longRegistered.put(toRegister, id);
        }

        if(factory instanceof PersistentDataIC && CraftBookPlugin.inst().getConfiguration().ICSavePersistentData) {
            try {
                if(((PersistentDataIC) factory).getStorageFile().exists())
                    ((PersistentDataIC) factory).loadPersistentData(new DataInputStream(new FileInputStream(((PersistentDataIC) factory).getStorageFile())));
            } catch (FileNotFoundException e) {
                BukkitUtil.printStacktrace(e);
            } catch (IOException e) {
                CraftBookPlugin.logger().severe("An invalid ic save file was found!");
                BukkitUtil.printStacktrace(e);
            }
        }

        return true;
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

        return registered.get(id.toLowerCase(Locale.ENGLISH));
    }

    /**
     * Checks if the IC Mechanic at the given point is cached. If not it will return false.
     *
     * @param pt of the ic
     *
     * @return true if ic is cached
     */
    public static boolean isCachedIC(Location pt) {

        return cachedICs.containsKey(pt);
    }

    /**
     * Gets the cached IC based on its location in the world. isCached should be checked before calling this method.
     *
     * @param pt of the ic
     *
     * @return cached ic.
     */
    public static IC getCachedIC(Location pt) {

        return cachedICs.get(pt);
    }

    /**
     * Adds the given IC to the cached IC list.
     *
     * @param pt of the ic
     * @param ic to add
     */
    public static void addCachedIC(Location pt, IC ic) {

        if (!CraftBookPlugin.inst().getConfiguration().ICCached) return;
        if(cachedICs.containsKey(pt)) return;
        CraftBookPlugin.logDebugMessage("Caching IC at: " + pt.toString(), "ic-cache");
        cachedICs.put(pt, ic);
    }

    /**
     * Removes the given IC from the cache list based on its location.
     *
     * @param pt of the ic
     *
     * @return the removed ic
     */
    public static IC removeCachedIC(Location pt) {

        if (cachedICs.containsKey(pt)) {
            CraftBookPlugin.logDebugMessage("Removing cached IC at: " + pt.toString(), "ic-cache");
            return cachedICs.remove(pt);
        }
        return null;
    }

    /**
     * Gets called when the IC gets unloaded. This method then takes care of clearing the IC from the cache.
     *
     * @param pt of the block break
     */
    public static void unloadIC(Location pt) {

        removeCachedIC(pt);
    }

    /**
     * Clears the IC cache.
     *
     */
    public static void emptyCache() {

        CraftBookPlugin.logDebugMessage("Culling cached IC list.", "ic-cache");
        cachedICs.clear();
    }

    /**
     * Gets the IC Cache map.
     */
    public static Map<Location, IC> getCachedICs() {

        return cachedICs;
    }

    public boolean hasCustomPrefix(String prefix) {

        return customPrefix.contains(prefix.toLowerCase(Locale.ENGLISH));
    }
}
