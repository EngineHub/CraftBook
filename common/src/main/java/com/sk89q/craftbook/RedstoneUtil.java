package com.sk89q.craftbook;

import org.bukkit.*;
import org.bukkit.block.*;


/**
 * Decorates bukkit's directional block power queries with a three-valued logic
 * that differenciates between the wiring that is unpowered and the absense of
 * wiring.
 * 
 * @author hash
 * 
 */
public abstract class RedstoneUtil {
    /**
     * Represents the power input state of a mechanism.
     */
    public enum Power { 
        /** No potential power source is connected.  (This may cause a mechanism to either default to its ON or OFF behavior or do something else entirely; it depends on the mechanism. */
        NA,
        /** At least one potential power source is connected, and at least power source is on. */  
        ON,
        /** At least one potential power source is connected, but zero are on. */
        OFF
    }
    
    /**
     * @param mech
     * @param face
     * @return Power.ON if the block on mech's face is a potential power source and is powered;
     *         Power.Off if the block on mech's face is a potential power source but it is not providing power;
     *         Power.NA if there is no potential power source at the given face.
     */
    public static Power isPowered(Block mech, BlockFace face) {
        Block pow = mech.getRelative(face);
        //debug(pow);
        if (isPotentialPowerSource(mech, pow)) {
            if (pow.isBlockPowered() || pow.isBlockIndirectlyPowered()) return Power.ON;
            return Power.OFF;
        }
        return Power.NA;
    }
    
    /**
     * @param pow
     * @return true if the pow block is a power conductor (in CraftBook, at this time we only consider this to be wires).
     */
    public static boolean isPotentialPowerSource(Block pow) {
        return (pow.getType() == Material.REDSTONE_WIRE);
        //return BlockType.isRedstoneBlock(pow.getTypeId());
    }
    
    /**
     * @param mech
     * @param pow
     * @return true if a mechanism in the mech block is able to receive power from the pow block (i.e. if it's a power conductor and if it has a sense of directionality it is also pointing at mech).
     */
    public static boolean isPotentialPowerSource(Block mech, Block pow) {
        //FIXME this method doesn't actually take direction into account which is its stated purpose
        return (pow.getType() == Material.REDSTONE_WIRE);
    }
    
    
    
    public static void debug(Block block) {
        System.out.println("block "+block+" power debug:");
        System.out.println("\tblock.isBlockPowered() : "+block.isBlockPowered());
        System.out.println("\tblock.isBlockIndirectlyPowered() : "+block.isBlockIndirectlyPowered());
        for (BlockFace bf : BlockFace.values()) {
            System.out.println("\tblock.isBlockFacePowered("+bf+") : "+block.isBlockFacePowered(bf));
            System.out.println("\tblock.getFace("+bf+").isBlockPowered() : "+block.getRelative(bf).isBlockPowered());
            System.out.println("\tblock.isBlockFaceIndirectlyPowered("+bf+") : "+block.isBlockFaceIndirectlyPowered(bf));
            System.out.println("\tblock.getFace("+bf+").isBlockIndirectlyPowered("+bf+") : "+block.getRelative(bf).isBlockIndirectlyPowered());
        }
        System.out.println();
    }
}
