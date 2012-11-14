package com.sk89q.craftbook.mech.area;
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

import com.sk89q.craftbook.bukkit.MechanismsPlugin;
import com.sk89q.craftbook.util.HistoryHashMap;
import com.sk89q.worldedit.data.DataException;
import org.bukkit.World;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Pattern;

/**
 * Used to load, save, and cache cuboid copies.
 *
 * @author sk89q, Silthus
 */
public class CopyManager {

    private static final CopyManager INSTANCE = new CopyManager();
    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-z0-9_]+$", Pattern.CASE_INSENSITIVE);

    /**
     * Cache.
     */
    private final HashMap<String, HistoryHashMap<String, CuboidCopy>> cache =
            new HashMap<String, HistoryHashMap<String, CuboidCopy>>();

    /**
     * Remembers missing copies so as to not look for them on disk.
     */
    private final HashMap<String, HistoryHashMap<String, Long>> missing =
            new HashMap<String, HistoryHashMap<String, Long>>();

    /**
     * Gets the copy manager instance
     *
     * @return The Copy Manager Instance
     */
    public static CopyManager getInstance() {

        return INSTANCE;
    }

    /**
     * Checks to see whether a name is a valid copy name.
     *
     * @param name
     *
     * @return
     */
    public static boolean isValidName(String name) {

        // name needs to be between 1 and 13 letters long so we can fit the - XXX - on the sides of the sign to
        // indicate what area is toggled on
        return !name.isEmpty() && name.length() <= 13 && NAME_PATTERN.matcher(name).matches();
    }

    /**
     * Checks to see whether a name is a valid namespace.
     *
     * @param name
     *
     * @return
     */
    public static boolean isValidNamespace(String name) {

        return !name.isEmpty() && name.length() <= 14 && NAME_PATTERN.matcher(name).matches();
    }

    /**
     * Checks if the area and namespace exists.
     *
     * @param plugin
     * @param namespace to check
     * @param area      to check
     */
    public static boolean isExistingArea(MechanismsPlugin plugin, String namespace, String area) {

        area = area.replace("-", "") + getFileSuffix(plugin);
        File file = new File(plugin.getDataFolder(), "areas/" + namespace);
        return new File(file, area).exists();
    }

    /**
     * Load a copy from disk. This may return a cached copy. If the copy is not
     * cached, the file will be loaded from disk if possible. If the copy does
     * not exist, an exception will be raised. An exception may be raised if the file
     * exists but cannot be read for whatever reason.
     *
     * @param namespace
     * @param id
     *
     * @return
     *
     * @throws IOException
     * @throws MissingCuboidCopyException
     * @throws CuboidCopyException
     */
    public CuboidCopy load(World world, String namespace, String id, MechanismsPlugin plugin)
            throws IOException, CuboidCopyException {

        id = id.toLowerCase();
        String cacheKey = namespace + "/" + id;

        HistoryHashMap<String, Long> missing = getMissing(world.getUID().toString());

        if (missing.containsKey(cacheKey)) {
            long lastCheck = missing.get(cacheKey);
            if (lastCheck > System.currentTimeMillis()) throw new MissingCuboidCopyException(id);
        }

        HistoryHashMap<String, CuboidCopy> cache = getCache(world.getUID().toString());

        CuboidCopy copy = cache.get(cacheKey);

        if (copy == null) {
            File folder = new File(new File(plugin.getDataFolder(), "areas"), namespace);
            copy = CuboidCopy.load(new File(folder, id + getFileSuffix(plugin)), world);
            missing.remove(cacheKey);
            cache.put(cacheKey, copy);
            return copy;
        }

        return copy;
    }

    /**
     * Save a copy to disk. The copy will be cached.
     *
     * @param id
     * @param copyFlat
     *
     * @throws IOException
     */
    public void save(World world, String namespace, String id, CuboidCopy copyFlat, MechanismsPlugin plugin)
            throws IOException, DataException {

        HistoryHashMap<String, CuboidCopy> cache = getCache(world.getUID().toString());

        File folder = new File(new File(plugin.getDataFolder(), "areas"), namespace);

        if (!folder.exists()) {
            folder.mkdirs();
        }

        id = id.toLowerCase();

        String cacheKey = namespace + "/" + id;

        copyFlat.save(new File(folder, id + getFileSuffix(plugin)));
        missing.remove(cacheKey);
        cache.put(cacheKey, copyFlat);
    }

    /**
     * Gets whether a copy can be made.
     *
     * @param namespace
     * @param ignore
     *
     * @return -1 if the copy can be made, some other number for the count
     */
    public int meetsQuota(World world, String namespace, String ignore, int quota, MechanismsPlugin plugin) {

        String ignoreFilename = ignore + getFileSuffix(plugin);

        String[] files = new File(new File(plugin.getDataFolder(), "areas"), namespace).list();

        if (files == null)
            return quota > 0 ? -1 : 0;
            else if (ignore == null)
                return files.length < quota ? -1 : files.length;
            else {
                int count = 0;

                for (String f : files) {
                    if (f.equals(ignoreFilename)) return -1;

                    count++;
                }

                return count < quota ? -1 : count;
            }
    }

    private HistoryHashMap<String, CuboidCopy> getCache(String world) {

        HistoryHashMap<String, CuboidCopy> worldCache = cache.get(world);
        if (worldCache != null) {
            return worldCache;
        } else {
            worldCache = new HistoryHashMap<String, CuboidCopy>(10);
            cache.put(world, worldCache);
            return worldCache;
        }
    }

    private HistoryHashMap<String, Long> getMissing(String world) {

        HistoryHashMap<String, Long> worldCache = missing.get(world);
        if (worldCache != null) {
            return worldCache;
        } else {
            worldCache = new HistoryHashMap<String, Long>(10);
            missing.put(world, worldCache);
            return worldCache;
        }
    }

    private static String getFileSuffix(MechanismsPlugin plugin) {

        return plugin.getLocalConfiguration().areaSettings.useSchematics ? ".schematic" : ".cbcopy";
    }
}