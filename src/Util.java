import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import com.sk89q.craftbook.InsufficientArgumentsException;
import com.sk89q.craftbook.Vector;
import com.sk89q.worldedit.blocks.BlockType;

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

/**
 * Library for Minecraft-related functions.
 * 
 * @author sk89q
 */
public class Util {

	/**
	 * Gets the block behind a sign.
	 *
	 * @param x
	 * @param y
	 * @param z
	 * @param multiplier
	 * @return
	 */
	public static Vector getWallSignBack(Vector pt, int multiplier) {
	    int x = pt.getBlockX();
	    int y = pt.getBlockY();
	    int z = pt.getBlockZ();
	    int data = CraftBook.getBlockData(x, y, z);
	    if (data == 0x2) { // East
	        return new Vector(x, y, z + multiplier);
	    } else if (data == 0x3) { // West
	        return new Vector(x, y, z - multiplier);
	    } else if (data == 0x4) { // North
	        return new Vector(x + multiplier, y, z);
	    } else {
	        return new Vector(x - multiplier, y, z);
	    }
	}

	/**
	 * Gets the block behind a sign.
	 *
	 * @param x
	 * @param y
	 * @param z
	 * @param multiplier
	 * @return
	 */
	public static Vector getSignPostOrthogonalBack(Vector pt, int multiplier) {
	    int x = pt.getBlockX();
	    int y = pt.getBlockY();
	    int z = pt.getBlockZ();
	    int data = CraftBook.getBlockData(x, y, z);
	    if (data == 0x8) { // East
	        return new Vector(x, y, z + multiplier);
	    } else if (data == 0x0) { // West
	        return new Vector(x, y, z - multiplier);
	    } else if (data == 0x4) { // North
	        return new Vector(x + multiplier, y, z);
	    } else if (data == 0xC) { // South
	        return new Vector(x - multiplier, y, z);
	    } else {
	        return null;
	    }
	}

	/**
	 * Gets the block next to a sign.
	 *
	 * @param x
	 * @param y
	 * @param z
	 * @param multiplier
	 * @return
	 */
	public static Vector getWallSignSide(Vector pt, int multiplier) {
	    int x = pt.getBlockX();
	    int y = pt.getBlockY();
	    int z = pt.getBlockZ();
	    int data = CraftBook.getBlockData(x, y, z);
	    if (data == 0x2) { // East
	        return new Vector(x + multiplier, y, z );
	    } else if (data == 0x3) { // West
	        return new Vector(x - multiplier, y, z);
	    } else if (data == 0x4) { // North
	        return new Vector(x, y, z - multiplier);
	    } else {
	        return new Vector(x, y, z + multiplier);
	    }
	}

	/**
	 * Checks whether a sign at a location has a certain text on a
	 * particular line, case in-sensitive.
	 * 
	 * @param pt
	 * @param lineNo
	 * @param text
	 * @return
	 */
	public static boolean doesSignSay(Vector pt, int lineNo, String text) {
	    ComplexBlock cBlock = etc.getServer().getComplexBlock(
	            pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
	
	    if (cBlock instanceof Sign) {
	        Sign sign = (Sign)cBlock;
	        return text.equalsIgnoreCase(sign.getText(lineNo));
	    }
	
	    return false;
	}

	/**
	 * Change a block ID to its name.
	 * 
	 * @param id
	 * @return
	 */
	public static String toBlockName(int id) {
	    com.sk89q.worldedit.blocks.BlockType blockType =
	            com.sk89q.worldedit.blocks.BlockType.fromID(id);
	
	    if (blockType == null) {
	        return "#" + id;
	    } else {
	        return blockType.getName();
	    }
	}

	/**
	 * Joins a string from an array of strings.
	 *
	 * @param str
	 * @param delimiter
	 * @return
	 */
	public static String joinString(String[] str, String delimiter,
	        int initialIndex) {
	    if (str.length == 0) {
	        return "";
	    }
	    StringBuilder buffer = new StringBuilder(str[initialIndex]);
	    for (int i = initialIndex + 1; i < str.length; i++) {
	        buffer.append(delimiter).append(str[i]);
	    }
	    return buffer.toString();
	}

	/**
	 * Repeat a string.
	 * 
	 * @param string
	 * @param num
	 * @return
	 */
	public static String repeatString(String str, int num) {
	    StringBuilder buffer = new StringBuilder();
	    for (int i = 0; i < num; i++) {
	        buffer.append(str);
	    }
	    return buffer.toString();
	}

	/**
	 * Convert a comma-delimited list to a set of integers.
	 *
	 * @param str
	 * @return
	 */
	public static Set<Integer> toBlockIDSet(String str) {
	    if (str.trim().length() == 0) {
	        return null;
	    }
	
	    String[] items = str.split(",");
	    Set<Integer> result = new HashSet<Integer>();
	
	    for (String item : items) {
	        try {
	            result.add(Integer.parseInt(item.trim()));
	        } catch (NumberFormatException e) {
	            int id = etc.getDataSource().getItem(item.trim());
	            if (id != 0) {
	                result.add(id);
	            } else {
	                CraftBookListener.logger.log(Level.WARNING, "CraftBook: Unknown block name: "
	                        + item);
	            }
	        }
	    }
	
	    return result;
	}

    /**
     * Checks to make sure that there are enough but not too many arguments.
     *
     * @param args
     * @param min
     * @param max -1 for no maximum
     * @param cmd command name
     * @throws InsufficientArgumentsException
     */
    public static void checkArgs(String[] args, int min, int max, String cmd)
            throws InsufficientArgumentsException {
        if (args.length <= min) {
            throw new InsufficientArgumentsException("Minimum " + min + " arguments");
        } else if (max != -1 && args.length - 1 > max) {
            throw new InsufficientArgumentsException("Maximum " + max + " arguments");
        }
    }
}
