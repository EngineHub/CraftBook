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

import com.sk89q.craftbook.*;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.File;

/**
 * Used to load, save, and cache cuboid copies.
 *
 * @author sk89q
 */
public class CopyManager {
    /**
     * Cache.
     */
    private HistoryHashMap<String,CuboidCopy> cache =
            new HistoryHashMap<String,CuboidCopy>(10);
    /**
     * Remembers missing copies so as to not look for them on disk.
     */
    private HistoryHashMap<String,Long> missing =
            new HistoryHashMap<String,Long>(20);

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
     * Load a copy from disk. This may return a cached copy. If the copy is not
     * cached, the file will be loaded from disk if possible. If the copy does
     * not exist, null will be returned. An exception may be raised if the file
     * exists but cannot be read for whatever reason.
     * 
     * @param id
     * @return
     * @throws IOException
     * @throws CuboidCopyException
     */
    public CuboidCopy load(String id) throws IOException, CuboidCopyException {
        id = id.toLowerCase();
        
        if (missing.containsKey(id)) {
            long lastCheck = missing.get(id);
            if (lastCheck > System.currentTimeMillis()) {
                return null;
            }
        }

        CuboidCopy copy = cache.get(id);

        if (copy == null) {
            try {
                copy = CuboidCopy.load("copyareas" + File.separator + id);
                missing.remove(id);
                cache.put(id, copy);
                return copy;
            } catch (IOException e2) { // Still raise the exception
                e2.printStackTrace();
                missing.put(id, System.currentTimeMillis() + 10000);
                throw e2;
            }
        }

        return copy;
    }

    /**
     * Save a copy to disk. The copy will be cached.
     * 
     * @param id
     * @param copy
     * @throws IOException
     */
    public void save(String id, CuboidCopy copy) throws IOException {
        File folder = new File("copyareas");
        if (!folder.exists()) {
            folder.mkdir();
        }

        id = id.toLowerCase();
        
        copy.save("copyareas" + File.separator + id);
        missing.remove(id);
        cache.put(id, copy);
    }
}