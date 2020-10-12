/*
 * CraftBook Copyright (C) me4502 <https://matthewmiller.dev/>
 * CraftBook Copyright (C) EngineHub and Contributors <https://enginehub.org/>
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

package org.enginehub.craftbook.util;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.enginehub.craftbook.CraftBook;
import org.enginehub.craftbook.bukkit.CraftBookPlugin;

/**
 * Decorates bukkit's directional block power queries with a three-valued logic that differentiates
 * between the wiring that is unpowered and the absence of wiring.
 */
public final class RedstoneUtil {

    private RedstoneUtil() {
    }

    /**
     * Represents the power input state of a mechanism.
     */
    public enum Power {
        /**
         * No potential power source is connected. (This may cause a mechanism to either default to
         * its ON or OFF
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
     * Gets whether this block is powered by this face.
     *
     * @param mechanicBlock The mechanic block
     * @param face The block face to check
     * @return The power state at the given face
     */
    public static Power isPowered(Block mechanicBlock, BlockFace face) {
        Block pow = mechanicBlock.getRelative(face);

        if (CraftBookPlugin.isDebugFlagEnabled("redstone")) {
            CraftBook.logger.info("block " + pow + " power debug:");
            CraftBook.logger.info("\tblock.isBlockPowered() : " + pow.isBlockPowered());
            CraftBook.logger.info("\tblock.isBlockIndirectlyPowered() : " + pow.isBlockIndirectlyPowered());
            for (BlockFace bf : BlockFace.values()) {
                CraftBook.logger.info("\tblock.isBlockFacePowered(" + bf + ") : " + pow.isBlockFacePowered(bf));
                CraftBook.logger.info("\tblock.getFace(" + bf + ").isBlockPowered() : " + pow.getRelative(bf)
                    .isBlockPowered());
                CraftBook.logger.info("\tblock.isBlockFaceIndirectlyPowered(" + bf + ") : " + pow
                    .isBlockFaceIndirectlyPowered(bf));
                CraftBook.logger.info("\tblock.getFace(" + bf + ").isBlockIndirectlyPowered(" + bf + ") : "
                    + pow.getRelative(bf).isBlockIndirectlyPowered());
            }
            CraftBook.logger.info("");
        }

        if (isPotentialPowerSource(pow.getType())) {
            return (pow.isBlockPowered() || pow.isBlockIndirectlyPowered()) ? Power.ON : Power.OFF;
        }
        return Power.NA;
    }

    /**
     * Gets whether this block is a potential power source.
     *
     * @return if this block is a potential power source
     */
    public static boolean isPotentialPowerSource(Material typeId) {
        return typeId == Material.REDSTONE_WIRE
            || typeId == Material.REPEATER
            || typeId == Material.LEVER
            || typeId == Material.REDSTONE_TORCH
            || typeId == Material.REDSTONE_WALL_TORCH
            || Tag.PRESSURE_PLATES.isTagged(typeId)
            || typeId == Material.COMPARATOR
            || typeId == Material.REDSTONE_BLOCK;
    }
}
