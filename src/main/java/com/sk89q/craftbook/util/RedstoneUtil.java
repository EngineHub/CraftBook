package com.sk89q.craftbook.util;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.worldedit.blocks.BlockID;

/**
 * Decorates bukkit's directional block power queries with a three-valued logic that differenciates between the
 * wiring that is unpowered and the
 * absense of wiring.
 *
 * @author hash
 */
public class RedstoneUtil {

    /**
     * Represents the power input state of a mechanism.
     */
    public enum Power {
        /**
         * No potential power source is connected. (This may cause a mechanism to either default to its ON or OFF
         * behavior or do something else
         * entirely; it depends on the mechanism.
         */
        NA,
        /**
         * At least one potential power source is connected, and at least power source is on.
         */
        ON,
        /**
         * At least one potential power source is connected, but zero are on.
         */
        OFF
    }

    /**
     * @param mech
     * @param face
     *
     * @return Power.ON if the block on mech's face is a potential power source and is powered; Power.Off if the
     *         block on mech's face is a potential
     *         power source but it is not providing power; Power.NA if there is no potential power source at the
     *         given face.
     */
    public static Power isPowered(Block mech, BlockFace face) {

        Block pow = mech.getRelative(face);

        if(CraftBookPlugin.isDebugFlagEnabled("redstone"))
            debug(pow);

        if (isPotentialPowerSource(mech, pow)) {
            if (pow.isBlockPowered() || pow.isBlockIndirectlyPowered()) return Power.ON;
            return Power.OFF;
        }
        return Power.NA;
    }

    /**
     * @return true if the pow block is a power conductor (in CraftBook, at this time we only consider this to be
     *         wires).
     */
    public static boolean isPotentialPowerSource(int typeId) {

        return typeId == BlockID.REDSTONE_WIRE || typeId == BlockID.REDSTONE_REPEATER_ON
                || typeId == BlockID.REDSTONE_REPEATER_OFF
                || typeId == BlockID.LEVER
                || typeId == BlockID.REDSTONE_TORCH_ON
                || typeId == BlockID.REDSTONE_TORCH_OFF
                || typeId == BlockID.WOODEN_PRESSURE_PLATE
                || typeId == BlockID.STONE_PRESSURE_PLATE
                || typeId == BlockID.PRESSURE_PLATE_LIGHT
                || typeId == BlockID.PRESSURE_PLATE_HEAVY
                || typeId == BlockID.COMPARATOR_OFF
                || typeId == BlockID.COMPARATOR_ON
                || typeId == BlockID.REDSTONE_BLOCK;
    }

    public static boolean isPotentialPowerSource(Block pow) {

        return isPotentialPowerSource(pow.getTypeId());
    }

    /**
     * @param mech
     * @param pow
     *
     * @return true if a mechanism in the mech block is able to receive power from the pow block (i.e. if it's a
     *         power conductor and if it has a sense
     *         of directionality it is also pointing at mech).
     */
    public static boolean isPotentialPowerSource(Block mech, Block pow) {

        return isPotentialPowerSource(pow);
    }

    public static void debug(Block block) {

        CraftBookPlugin.inst().getLogger().info("block " + block + " power debug:");
        CraftBookPlugin.inst().getLogger().info("\tblock.isBlockPowered() : " + block.isBlockPowered());
        CraftBookPlugin.inst().getLogger().info("\tblock.isBlockIndirectlyPowered() : " + block.isBlockIndirectlyPowered());
        for (BlockFace bf : BlockFace.values()) {
            CraftBookPlugin.inst().getLogger().info("\tblock.isBlockFacePowered(" + bf + ") : " + block.isBlockFacePowered(bf));
            CraftBookPlugin.inst().getLogger().info("\tblock.getFace(" + bf + ").isBlockPowered() : " + block.getRelative(bf)
                    .isBlockPowered());
            CraftBookPlugin.inst().getLogger().info("\tblock.isBlockFaceIndirectlyPowered(" + bf + ") : " + block
                    .isBlockFaceIndirectlyPowered(bf));
            CraftBookPlugin.inst().getLogger().info("\tblock.getFace(" + bf + ").isBlockIndirectlyPowered(" + bf + ") : "
                    + block.getRelative(bf).isBlockIndirectlyPowered());
        }
        CraftBookPlugin.inst().getLogger().info("");
    }
}