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

/**
 * Used to load, save, and cache cuboid copies.
 *
 * @author sk89q
 */
public class CopyManager {

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
	 * Checks to see whether a name is a valid copy name.
	 *
	 * @param name
	 * @return
	 */
	public static boolean isValidName(String name) {

		return name.length() > 0 && name.length() <= 30
				&& name.matches("^[A-Za-z0-9_\\- ]+$");
	}

	/**
	 * Checks to see whether a name is a valid namespace.
	 *
	 * @param name
	 * @return
	 */
	public static boolean isValidNamespace(String name) {

		return name.length() > 0 && name.length() <= 15
				&& name.matches("^[A-Za-z0-9_]+$");
	}

	/**
	 * Load a copy from disk. This may return a cached copy. If the copy is not
	 * cached, the file will be loaded from disk if possible. If the copy does
	 * not exist, an exception will be raised. An exception may be raised if the file
	 * exists but cannot be read for whatever reason.
	 *
	 * @param namespace
	 * @param id
	 * @return
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
			if (lastCheck > System.currentTimeMillis()) {
				throw new MissingCuboidCopyException(id);
			}
		}

		HistoryHashMap<String, CuboidCopy> cache = getCache(world.getUID().toString());

        CuboidCopy copy = cache.get(id);

		if (copy == null) {
            File folder = new File(new File(plugin.getDataFolder(), "areas"), namespace);
            if (plugin.getLocalConfiguration().areaSettings.useSchematics) {
                copy = CuboidCopy.load(new File(folder, id + ".schematic"));
            } else {
                copy = CuboidCopy.load(new File(folder, id + ".cbcopy"));
            }
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
	 * @throws IOException
	 */
	public void save(World world, String namespace, String id, CuboidCopy copyFlat, MechanismsPlugin plugin)
            throws IOException, DataException {

		HistoryHashMap<String, CuboidCopy> cache = getCache(world.getUID().toString());

		File folder = new File(new File(plugin.getDataFolder(), "areas"), namespace);

		if (!folder.exists()) folder.mkdirs();

		id = id.toLowerCase();

		String cacheKey = namespace + "/" + id;

		copyFlat.save(new File(folder, id + ".cbcopy"));
		missing.remove(cacheKey);
		cache.put(id, copyFlat);
	}

	/**
	 * Gets whether a copy can be made.
	 *
	 * @param namespace
	 * @param ignore
	 * @return -1 if the copy can be made, some other number for the count
	 */
	public int meetsQuota(World world, String namespace, String ignore, int quota, MechanismsPlugin plugin) {

		String ignoreFilename = ignore + ".cbcopy";

		String[] files = new File(new File(plugin.getDataFolder(), "areas"), namespace).list();

		if (files == null) {
			return quota > 0 ? -1 : 0;
		} else if (ignore == null) {
			return files.length < quota ? -1 : files.length;
		} else {
			int count = 0;

			for (String f : files) {
				if (f.equals(ignoreFilename)) {
					return -1;
				}

				count++;
			}

			return count < quota ? -1 : count;
		}
	}

	private HistoryHashMap<String, CuboidCopy> getCache(String world) {

		if (cache.containsKey(world)) return cache.get(world);
		else {
			HistoryHashMap<String, CuboidCopy> h = new HistoryHashMap<String, CuboidCopy>(10);
			cache.put(world, h);
			return h;
		}
	}

	private HistoryHashMap<String, Long> getMissing(String world) {

		if (missing.containsKey(world)) return missing.get(world);
		else {
			HistoryHashMap<String, Long> h = new HistoryHashMap<String, Long>(10);
			missing.put(world, h);
			return h;
		}
	}
}