package com.sk89q.craftbook.util;

import org.bukkit.block.*;

/**
 * <p>
 * Convenience methods for dealing with some signpost data.
 * </p>
 * 
 * <p>
 * If you intend to care about the eight further directions (as opposed to the
 * four cardinal directions and the four ordinal directions), this isn't for you
 * -- since BlockFace has no such directions, those will be rounded to the
 * nearest cardinal direction.
 * </p>
 * 
 * @author hash
 * 
 */
// it might make sense to put this in the SignOrientedMechanic class later; unsure.
public class SignUtil {
    /**
     * @param signpost
     *            if this isn't a signpost, you'll get weird data, but no errors
     * @return the direction a player would be facing when reading the signpost;
     *         i.e. the face that is actually the back side of the signpost.
     */
    public static BlockFace getFacing(Block signpost) {
        return getBack(signpost);
    }

    public static BlockFace getFront(Block signpost) {
        switch (signpost.getData()) {
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
    }

    public static BlockFace getBack(Block signpost) {
        switch (signpost.getData()) {
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
    }

    public static BlockFace getLeft(Block signpost) {
        switch (signpost.getData()) {
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
    }

    public static BlockFace getRight(Block signpost) {
        switch (signpost.getData()) {
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
    }
    
    public static boolean isCardinal(Block signpost) {
        switch (signpost.getData()) {
        case 0x0:
        case 0x4:
        case 0x8:
        case 0xC:
                return true;
        default:
                return false;
        }
    }
}
