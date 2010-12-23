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

import java.io.FileNotFoundException;
import java.io.IOException;
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
     * not exist, null will be returned. An exception may be raised if the file
     * exists but cannot be read for whatever reason.
     * 
     * @param namespace
     * @param id
     * @return
     * @throws IOException
     * @throws CuboidCopyException
     */
    public CuboidCopy load(String namespace, String id)
    		throws IOException, CuboidCopyException {
    	
        id = id.toLowerCase();
        String cacheKey = namespace + "/" + id;
        
        if (missing.containsKey(cacheKey)) {
            long lastCheck = missing.get(cacheKey);
            if (lastCheck > System.currentTimeMillis()) {
                return null;
            }
        }

        CuboidCopy copy = cache.get(id);

        if (copy == null) {
            try {
                copy = CuboidCopy.load("world" + File.separator
                		+ "craftbook" + File.separator
                		+ "areas" + File.separator
                		+ namespace + File.separator
                		+ id + ".cbcopy");
                missing.remove(cacheKey);
                cache.put(cacheKey, copy);
                return copy;
            } catch (FileNotFoundException e) {
                missing.put(cacheKey, System.currentTimeMillis() + 10000);
            } catch (IOException e) {
                missing.put(cacheKey, System.currentTimeMillis() + 10000);
                throw e;
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
    public void save(String namespace, String id, CuboidCopy copy)
    		throws IOException {
    	
        File folder = new File("world" + File.separator
    			+ "craftbook" + File.separator
    			+ "areas" + File.separator
    			+ namespace);
        
        if (!folder.exists()) {
            folder.mkdirs();
        }

        id = id.toLowerCase();

        String cacheKey = namespace + "/" + id;
        
        copy.save(new File(folder, id + ".cbcopy"));
        missing.remove(cacheKey);
        cache.put(id, copy);
    }

    /**
     * Gets whether a copy can be made.
     * 
     * @param namespace
     * @param ignore
     * @return -1 if the copy can be made, some other number for the count
     */
    public int meetsQuota(String namespace, String ignore, int quota) {
    	String ignoreFilename = ignore + ".cbcopy";
    	
    	String[] files = new File("world" + File.separator
    			+ "craftbook" + File.separator
    			+ "areas" + File.separator
    			+ namespace).list();
    	
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
    		
    		return count;
    	}
    }
}