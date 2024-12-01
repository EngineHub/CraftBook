/*
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

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.enginehub.craftbook.CraftBook;
import org.enginehub.craftbook.bukkit.CraftBookPlugin;
import org.jspecify.annotations.Nullable;

public final class ProtectionUtil {

    private ProtectionUtil() {
    }

    /**
     * Checks to see if a player can build at a location. This will return
     * true if region protection is disabled or WorldGuard is not found.
     *
     * @param player The player to check
     * @param block The block to check at.
     * @return whether {@code player} can build at {@code block}'s location
     */
    public static boolean canBuild(Player player, Block block) {
        // WG Checks
        if (CraftBook.getInstance().getPlatform().getConfiguration().obeyWorldGuard && CraftBookPlugin.plugins.getWorldGuard() != null) {
            if (!CraftBookPlugin.plugins.getWorldGuard().createProtectionQuery().testBlockPlace(player, block.getLocation(), block.getType())) {
                return false;
            }
        }

        // Generic plugin checks
        if (CraftBook.getInstance().getPlatform().getConfiguration().obeyPluginProtections) {
            BlockPlaceEvent event = new BlockPlaceEvent(block, block.getState(), block.getRelative(0, -1, 0), player.getInventory().getItemInMainHand(), player, true, EquipmentSlot.HAND);
            EventUtil.callEventSafely(event);
            return !event.isCancelled();
        }

        return true;
    }

    /**
     * Checks to see if a player can break blocks at a location. This will return
     * true if region protection is disabled or WorldGuard is not found.
     *
     * @param player The player to check
     * @param block The block to check at.
     * @return whether {@code player} can break blocks at {@code block}'s location
     */
    public static boolean canBreak(Player player, Block block) {
        // WG Checks
        if (CraftBook.getInstance().getPlatform().getConfiguration().obeyWorldGuard && CraftBookPlugin.plugins.getWorldGuard() != null) {
            if (!CraftBookPlugin.plugins.getWorldGuard().createProtectionQuery().testBlockBreak(player, block)) {
                return false;
            }
        }

        // Generic plugin checks
        if (CraftBook.getInstance().getPlatform().getConfiguration().obeyPluginProtections) {
            BlockBreakEvent event = new BlockBreakEvent(block, player);
            EventUtil.callEventSafely(event);
            return !event.isCancelled();
        }

        return true;
    }

    public static boolean canSendCommand(Player player, String command) {
        if (CraftBook.getInstance().getPlatform().getConfiguration().obeyPluginProtections) {
            PlayerCommandPreprocessEvent event = new PlayerCommandPreprocessEvent(player, command);
            EventUtil.callEventSafely(event);
            return !event.isCancelled();
        }
        return true;
    }

    /**
     * Checks to see if a player can use at a location. This will return
     * true if region protection is disabled or WorldGuard is not found.
     *
     * @param player The player to check.
     * @param loc The location to check at.
     * @param face The face of the block being interacted with.
     * @param action The action being performed.
     * @return whether {@code player} can build at {@code loc}
     */
    public static boolean canUse(Player player, Location loc, @Nullable BlockFace face, @Nullable Action action) {
        // WG Checks
        if (CraftBook.getInstance().getPlatform().getConfiguration().obeyWorldGuard && CraftBookPlugin.plugins.getWorldGuard() != null) {
            if (!CraftBookPlugin.plugins.getWorldGuard().createProtectionQuery().testBlockInteract(player, loc.getBlock())) {
                // If WorldGuard says no, we don't need to check anything else.
                return false;
            }
        }

        // Generic plugin checks
        if (CraftBook.getInstance().getPlatform().getConfiguration().obeyPluginProtections) {
            PlayerInteractEvent event = new PlayerInteractEvent(player, action == null ? Action.RIGHT_CLICK_BLOCK : action, player.getInventory().getItemInMainHand(), loc.getBlock(), face == null ? BlockFace.SELF : face);
            EventUtil.callEventSafely(event);
            return !event.isCancelled();
        }

        return true;
    }

    /**
     * Checks to see if a player can use at a location. This will return
     * true if region protection is disabled or WorldGuard is not found.
     *
     * @param player The player to check.
     * @param block The location to check at.
     * @return whether {@code player} can build at {@code loc}
     */
    public static boolean canAccessInventory(Player player, Block block) {
        return canUse(player, block.getLocation(), null, Action.RIGHT_CLICK_BLOCK);
    }

    /**
     * Checks to see if a block can form at a specific location. This will
     * return true if region protection is disabled or WorldGuard is not found.
     *
     * @param block The block that is changing.
     * @param newState The new state of the block.
     * @return Whether the block can form.
     */
    public static boolean canBlockForm(Block block, BlockState newState) {
        // WG Checks
        if (CraftBook.getInstance().getPlatform().getConfiguration().obeyWorldGuard && CraftBookPlugin.plugins.getWorldGuard() != null) {
            Material newType = newState.getType();

            if (newType == Material.SNOW || newType == Material.ICE || newType == Material.FROSTED_ICE) {
                com.sk89q.worldguard.protection.regions.RegionContainer container = com.sk89q.worldguard.WorldGuard.getInstance().getPlatform().getRegionContainer();
                com.sk89q.worldguard.protection.managers.RegionManager manager = container.get(BukkitAdapter.adapt(block.getWorld()));

                if (manager != null) {
                    com.sk89q.worldguard.protection.ApplicableRegionSet regionSet = manager.getApplicableRegions(BukkitAdapter.asBlockVector(block.getLocation()));

                    com.sk89q.worldguard.protection.flags.StateFlag flag = switch(newType) {
                        case SNOW -> com.sk89q.worldguard.protection.flags.Flags.SNOW_FALL;
                        case ICE -> com.sk89q.worldguard.protection.flags.Flags.ICE_FORM;
                        case FROSTED_ICE -> com.sk89q.worldguard.protection.flags.Flags.FROSTED_ICE_FORM;
                        default -> null;
                    };

                    if (!regionSet.testState(null, flag)) {
                        return false;
                    }
                }
            } else if (!CraftBookPlugin.plugins.getWorldGuard().createProtectionQuery().testBlockPlace(null, block.getLocation(), newType)) {
                return false;
            }
        }

        // Generic plugin checks
        if (CraftBook.getInstance().getPlatform().getConfiguration().obeyPluginProtections) {
            BlockFormEvent event = new BlockFormEvent(block, newState);
            EventUtil.callEventSafely(event);
            return !event.isCancelled();
        }

        return true;
    }

    /**
     * Checks whether protection related code should even be tested.
     *
     * <p>
     * This should only be used in cases where gathering required data to perform these checks is expensive, the actual
     * check functions within this class do not need to be guarded with this method on their own.
     * </p>
     *
     * @return whether to check or not
     */
    public static boolean shouldUseProtection() {
        return CraftBook.getInstance().getPlatform().getConfiguration().obeyPluginProtections
            || (CraftBook.getInstance().getPlatform().getConfiguration().obeyWorldGuard && CraftBookPlugin.plugins.getWorldGuard() != null);
    }
}
