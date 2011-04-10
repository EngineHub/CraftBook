package com.sk89q.craftbook.util;

import org.bukkit.*;
import org.bukkit.block.*;

import com.sk89q.worldedit.blocks.*;

/**
 * <p>
 * Convenience methods for dealing with some sign block data.
 * </p>
 * 
 * <p>
 * If you intend to care about the eight further directions (as opposed to the
 * four cardinal directions and the four ordinal directions), this isn't for you
 * -- since BlockFace has no such directions, those will be rounded to the
 * nearest ordinal direction. (If the term "further direction" confuses you,
 * see https://secure.wikimedia.org/wikipedia/en/wiki/Cardinal_directions).
 * </p>
 * 
 * <p>
 * This is direly close to being a replicate of things you can access via
 * org.bukkit.material.Sign (which extends MaterialData). However, that thing:
 * <ul>
 * <li>doesn't provide the relative direction methods.
 * <li>rounds the further divisions to cardinal/ordinal differently (and wrong,
 * in my book).
 * <li>has the same class name for that MaterialData thing as the BlockState,
 * which is annoying as hell import-wise.
 * <li>requires allocating an object and copying two bytes in a fashion that I
 * consider kinda unnecessary.
 * </ul>
 * Ideally, I think I'd like to see if I can get something like these methods
 * pushed to bukkit.
 * </p>
 * 
 * @author hash
 * 
 */
public class SignUtil {
    public static boolean isSign(Block keith) {
        return (keith.getTypeId() == BlockID.SIGN_POST || keith.getTypeId() == BlockID.WALL_SIGN);
    }
    
    /**
     * @param sign
     *            treated as sign post if it is such, or else assumed to be a
     *            wall sign (i.e., if you ask about a stone block, it's
     *            considered a wall sign).
     * @return the direction a player would be facing when reading the sign;
     *         i.e. the face that is actually the back side of the sign.
     */
    public static BlockFace getFacing(Block sign) {
        return getBack(sign);
    }
    
    /**
     * @param sign
     *            treated as sign post if it is such, or else assumed to be a
     *            wall sign (i.e., if you ask about a stone block, it's
     *            considered a wall sign).
     * @return the side of the sign containing the text (in other words, when a
     *         player places a new sign, while facing north, this will return
     *         south).
     */
    public static BlockFace getFront(Block sign) {
        if (sign.getType() == Material.SIGN_POST)
            switch (sign.getData()) {
            case 0x0:
                    return BlockFace.WEST;
            case 0x1:case 0x2:case 0x3:
                    return BlockFace.NORTH_WEST;
            case 0x4:
                    return BlockFace.NORTH;
            case 0x5:case 0x6:case 0x7:
                    return BlockFace.NORTH_EAST;
            case 0x8:
                    return BlockFace.EAST;
            case 0x9:case 0xA:case 0xB:
                    return BlockFace.SOUTH_EAST;
            case 0xC:
                 return BlockFace.SOUTH;
            case 0xD:case 0xE:case 0xF:
                    return BlockFace.SOUTH_WEST;
            default:
                    return BlockFace.SELF;
            }
        else
            switch (sign.getData()) {
            case 0x2:
                    return BlockFace.EAST;
            case 0x3:
                    return BlockFace.WEST;
            case 0x4:
                    return BlockFace.NORTH;
            case 0x5:
                    return BlockFace.SOUTH;
            default:
                    return BlockFace.SELF;
            }
    }
    
    public static Block getFrontBlock(Block sign) {
        return sign.getRelative(getFront(sign));
    }
    
    /**
     * @param sign
     *            treated as sign post if it is such, or else assumed to be a
     *            wall sign (i.e., if you ask about a stone block, it's
     *            considered a wall sign).
     * @return the blank side of the sign opposite the text. In the case of a
     *         wall sign, the block in this direction is the block to which the
     *         sign is attached. This is also the direction a player would be
     *         facing when reading the sign; see {@link #getFacing(Block)}.
     * 
     */
    public static BlockFace getBack(Block sign) {
        if (sign.getType() == Material.SIGN_POST)
            switch (sign.getData()) {
            case 0x0:
                    return BlockFace.EAST;
            case 0x1:case 0x2:case 0x3:
                    return BlockFace.SOUTH_EAST;
            case 0x4:
                    return BlockFace.SOUTH;
            case 0x5:case 0x6:case 0x7:
                    return BlockFace.SOUTH_WEST;
            case 0x8:
                    return BlockFace.WEST;
            case 0x9:case 0xA:case 0xB:
                    return BlockFace.NORTH_WEST;
            case 0xC:
                    return BlockFace.NORTH;
            case 0xD:case 0xE:case 0xF:
                    return BlockFace.NORTH_EAST;
            default:
                return BlockFace.SELF;
        }
        else
            switch (sign.getData()) {
            case 0x2:
                    return BlockFace.WEST;
            case 0x3:
                    return BlockFace.EAST;
            case 0x4:
                    return BlockFace.SOUTH;
            case 0x5:
                    return BlockFace.NORTH;
            default:
                return BlockFace.SELF;
            }
    }
    
    public static Block getBackBlock(Block sign) {
        return sign.getRelative(getBack(sign));
    }
    
    /**
     * @param sign
     *            treated as sign post if it is such, or else assumed to be a
     *            wall sign (i.e., if you ask about a stone block, it's
     *            considered a wall sign).
     * @return the cardinal or ordinal direction to a player's left as they face
     *         the sign to read it; if the sign is oriented in a further
     *         direction, the result is rounded to the nearest ordinal
     *         direction.
     */
    public static BlockFace getLeft(Block sign) {
        if (sign.getType() == Material.SIGN_POST)
            switch (sign.getData()) {
            case 0x0:
                    return BlockFace.SOUTH;
            case 0x1:case 0x2:case 0x3:
                    return BlockFace.SOUTH_WEST;
            case 0x4:
                    return BlockFace.WEST;
            case 0x5:case 0x6:case 0x7:
                    return BlockFace.NORTH_WEST;
            case 0x8:
                    return BlockFace.NORTH;
            case 0x9:case 0xA:case 0xB:
                    return BlockFace.NORTH_EAST;
            case 0xC:
                    return BlockFace.EAST;
            case 0xD:case 0xE:case 0xF:
                    return BlockFace.SOUTH_EAST;
            default:
                    return BlockFace.SELF;
            }
        else
            switch (sign.getData()) {
            case 0x2:
                    return BlockFace.NORTH;
            case 0x3:
                    return BlockFace.SOUTH;
            case 0x4:
                    return BlockFace.EAST;
            case 0x5:
                    return BlockFace.WEST;
            default:
                    return BlockFace.SELF;
            }
    }
    
    public static Block getLeftBlock(Block sign) {
        return sign.getRelative(getLeft(sign));
    }
    
    /**
     * @param sign
     *            treated as sign post if it is such, or else assumed to be a
     *            wall sign (i.e., if you ask about a stone block, it's
     *            considered a wall sign).
     * @return the cardinal or ordinal direction to a player's right they face
     *         the sign to read it; if the sign is oriented in a further
     *         direction, the result is rounded to the nearest ordinal
     *         direction.
     */
    public static BlockFace getRight(Block sign) {
        if (sign.getType() == Material.SIGN_POST)
            switch (sign.getData()) {
            case 0x0:
                    return BlockFace.NORTH;
            case 0x1:case 0x2:case 0x3:
                    return BlockFace.NORTH_EAST;
            case 0x4:
                    return BlockFace.EAST;
            case 0x5:case 0x6:case 0x7:
                    return BlockFace.SOUTH_EAST;
            case 0x8:
                    return BlockFace.SOUTH;
            case 0x9:case 0xA:case 0xB:
                    return BlockFace.SOUTH_WEST;
            case 0xC:
                    return BlockFace.WEST;
            case 0xD:case 0xE:case 0xF:
                    return BlockFace.NORTH_WEST;
            default:
                    return BlockFace.SELF;
            }
        else
            switch (sign.getData()) {
            case 0x2:
                    return BlockFace.SOUTH;
            case 0x3:
                    return BlockFace.NORTH;
            case 0x4:
                    return BlockFace.WEST;
            case 0x5:
                    return BlockFace.EAST;
            default:
                    return BlockFace.SELF;
            }
    }
    
    public static Block getRightBlock(Block sign) {
        return sign.getRelative(getRight(sign));
    }
    
    /**
     * @param sign
     *            treated as sign post if it is such, or else assumed to be a
     *            wall sign (i.e., if you ask about a stone block, it's
     *            considered a wall sign).
     * @return true if the sign is oriented along a cardinal direction (or if
     *         it's a wall sign, since those are always oriented along cardinal
     *         directions); false otherwise.
     */
    public static boolean isCardinal(Block sign) {
        if (sign.getType() == Material.SIGN_POST)
            switch (sign.getData()) {
            case 0x0:case 0x4:case 0x8:case 0xC:
                    return true;
            default:
                    return false;
            }
        else
            return true;
    }
    
    /**
     * @param BlockFace
     *            Start from direction
     * @return clockwise direction
     */
    public static BlockFace getClockWise(BlockFace yourFace) 
    {
        switch (yourFace) 
        {
	        case NORTH: return BlockFace.EAST;
	        case EAST: return BlockFace.SOUTH;
	        case SOUTH: return BlockFace.WEST;
	        case WEST: return BlockFace.NORTH;
	
			default: return BlockFace.SELF;
		}
    }
    
    /**
     * @param BlockFace
     *            Start from direction
     * @return clockwise direction
     */
    public static BlockFace getCounterClockWise(BlockFace yourFace) 
    {
        switch (yourFace) 
        {
	        case NORTH: return BlockFace.WEST;
	        case EAST: return BlockFace.NORTH;
	        case SOUTH: return BlockFace.EAST;
	        case WEST: return BlockFace.SOUTH;
	
			default: return BlockFace.SELF;
		}
    }
    
    
    
}
