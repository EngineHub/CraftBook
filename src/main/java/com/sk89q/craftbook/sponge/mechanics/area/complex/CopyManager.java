/*
 * CraftBook Copyright (C) 2010-2016 sk89q <http://www.sk89q.com>
 * CraftBook Copyright (C) 2011-2016 me4502 <http://www.me4502.com>
 * CraftBook Copyright (C) Contributors
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
package com.sk89q.craftbook.sponge.mechanics.area.complex;

import com.sk89q.craftbook.sponge.CraftBookPlugin;
import com.sk89q.worldedit.world.DataException;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.api.world.World;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.regex.Pattern;

public class CopyManager {

    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-z0-9_]+$", Pattern.CASE_INSENSITIVE);

    /**
     * Checks to see whether a name is a valid copy name.
     *
     * @param name The name to check
     *
     * @return If it is a valid name
     */
    public static boolean isValidName(String name) {
        // name needs to be between 1 and 13 letters long so we can fit the
        return !name.isEmpty() && name.length() <= 13 && NAME_PATTERN.matcher(name).matches();
    }

    /**
     * Checks if the area and namespace exists.
     *
     * @param namespace to check
     * @param area to check
     *
     * @return If the area exists
     */
    static boolean isExistingArea(File dataFolder, String namespace, String area) {
        area = StringUtils.replace(area, "-", "") + getFileSuffix();
        File file = new File(dataFolder, "areas/" + namespace);
        return new File(file, area).exists();
    }

    /**
     * Load a copy from disk. This may return a cached copy. If the copy is not cached,
     * the file will be loaded from disk if possible. If the copy
     * does not exist, an exception will be raised. An exception may be raised if the file exists but cannot be read
     * for whatever reason.
     *
     * @param world The world to load it into
     * @param namespace The namespace
     * @param id The area ID
     *
     * @return The CuboidCopy
     *
     * @throws CuboidCopyException Thrown if the data was invalid
     */
    public static CuboidCopy load(World world, String namespace, String id) throws CuboidCopyException {
        id = id.toLowerCase(Locale.ENGLISH);

        File folder = new File(new File(CraftBookPlugin.inst().getWorkingDirectory(), "areas"), namespace);
        return CuboidCopy.load(new File(folder, id + getFileSuffix()), world);
    }

    /**
     * Save a copy to disk. The copy will be cached.
     *
     * @param namespace The namespace
     * @param id The area ID
     * @param copy The CuboidCopy to save
     *
     * @throws IOException Thrown if an IO error occured when saving
     * @throws DataException Thrown if the data was invalid
     */
    public static void save(String namespace, String id, CuboidCopy copy) throws IOException, DataException {

        File folder = new File(new File(CraftBookPlugin.inst().getWorkingDirectory(), "areas"), namespace);

        if (!folder.exists()) {
            folder.mkdirs();
        }

        id = id.toLowerCase(Locale.ENGLISH);

        copy.save(new File(folder, id + getFileSuffix()));
    }

    /**
     * Gets whether a copy can be made.
     *
     * @param namespace The namespace
     * @param ignore A filename to ignore
     * @param quota The maximum quota
     *
     * @return -1 if the copy can be made, some other number for the count
     */
    public static int meetsQuota(String namespace, String ignore, int quota) {
        String ignoreFilename = ignore + getFileSuffix();

        String[] files = new File(new File(CraftBookPlugin.inst().getWorkingDirectory(), "areas"), namespace).list();

        if (files == null) return quota > 0 ? -1 : 0;
        else if (ignore == null) return files.length < quota ? -1 : files.length;
        else {
            int count = 0;

            for (String f : files) {
                if (f.equals(ignoreFilename)) return -1;

                count++;
            }

            return count < quota ? -1 : count;
        }
    }

    /**
     * Gets the suffix that the schematic files contain.
     *
     * @return The file suffix
     */
    public static String getFileSuffix() {
        return ".schematic";
    }
}