package com.sk89q.craftbook.util;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public final class ProtectionUtil {

    /**
     * Checks to see if a player can build at a location. This will return
     * true if region protection is disabled.
     *
     * @param player The player to check.
     * @param loc    The location to check at.
     * @param build True for build, false for break
     *
     * @return whether {@code player} can build at {@code loc}
     */
    public static boolean canBuild(Player player, Location loc, boolean build) {

        return canBuild(player,loc.getBlock(), build);
    }

    /**
     * Checks to see if a player can build at a location. This will return
     * true if region protection is disabled or WorldGuard is not found.
     *
     * @param player The player to check
     * @param block  The block to check at.
     * @param build True for build, false for break
     *
     * @return whether {@code player} can build at {@code block}'s location
     */
    public static boolean canBuild(Player player, Block block, boolean build) {

        if (!shouldUseProtection()) return true;
        if (CraftBookPlugin.inst().getConfiguration().advancedBlockChecks) {
            CompatabilityUtil.disableInterferences(player);
            BlockEvent event;
            if (build)
                event = new BlockPlaceEvent(block, block.getState(), block.getRelative(0, -1, 0), player.getInventory().getItemInMainHand(), player, true, EquipmentSlot.HAND);
            else
                event = new BlockBreakEvent(block, player);
            EventUtil.ignoreEvent(event);
            CraftBookPlugin.inst().getServer().getPluginManager().callEvent(event);
            CompatabilityUtil.enableInterferences(player);
            return !(((Cancellable) event).isCancelled() || event instanceof BlockPlaceEvent && !((BlockPlaceEvent) event).canBuild());
        }
        return !CraftBookPlugin.inst().getConfiguration().obeyWorldguard || (CraftBookPlugin.plugins.getWorldGuard() == null || build ? CraftBookPlugin.plugins.getWorldGuard().createProtectionQuery().testBlockPlace(player, block.getLocation(), block.getType()) : CraftBookPlugin.plugins.getWorldGuard().createProtectionQuery().testBlockBreak(player, block));

    }

    public static boolean canSendCommand(Player player, String command) {
        if (!shouldUseProtection()) return true;
        if (CraftBookPlugin.inst().getConfiguration().advancedBlockChecks) {
            CompatabilityUtil.disableInterferences(player);
            PlayerCommandPreprocessEvent event = new PlayerCommandPreprocessEvent(player, command);
            EventUtil.ignoreEvent(event);
            CraftBookPlugin.inst().getServer().getPluginManager().callEvent(event);
            CompatabilityUtil.enableInterferences(player);
            return !event.isCancelled();
        }
        return true;
    }

    /**
     * Checks to see if a player can use at a location. This will return
     * true if region protection is disabled or WorldGuard is not found.
     *
     * @param player The player to check.
     * @param loc    The location to check at.
     *
     * @return whether {@code player} can build at {@code loc}
     */
    public static boolean canUse(Player player, Location loc, BlockFace face, Action action) {

        if (!shouldUseProtection()) return true;
        if (CraftBookPlugin.inst().getConfiguration().advancedBlockChecks) {
            CompatabilityUtil.disableInterferences(player);
            PlayerInteractEvent event = new PlayerInteractEvent(player, action == null ? Action.RIGHT_CLICK_BLOCK : action, player.getItemInHand(), loc.getBlock(), face == null ? BlockFace.SELF : face);
            EventUtil.ignoreEvent(event);
            CraftBookPlugin.inst().getServer().getPluginManager().callEvent(event);
            CompatabilityUtil.enableInterferences(player);
            if (!event.isCancelled() && CraftBookPlugin.inst().getConfiguration().obeyWorldguard && CraftBookPlugin.plugins.getWorldGuard() != null) {
                return CraftBookPlugin.plugins.getWorldGuard().createProtectionQuery().testBlockInteract(player, loc.getBlock());
            }
            return !event.isCancelled();
        }
        return !CraftBookPlugin.inst().getConfiguration().obeyWorldguard || CraftBookPlugin.plugins.getWorldGuard() == null || CraftBookPlugin.plugins.getWorldGuard().createProtectionQuery().testBlockInteract(player, loc.getBlock());
    }

    /**
     * Checks to see if a player can use at a location. This will return
     * true if region protection is disabled or WorldGuard is not found.
     *
     * @param player The player to check.
     * @param block    The location to check at.
     *
     * @return whether {@code player} can build at {@code loc}
     */
    public static boolean canAccessInventory(Player player, Block block) {

        if (!shouldUseProtection()) return true;
        if (CraftBookPlugin.inst().getConfiguration().advancedBlockChecks) {

            if (!canUse(player, block.getLocation(), null, Action.RIGHT_CLICK_BLOCK))
                return false;
        }
        return !CraftBookPlugin.inst().getConfiguration().obeyWorldguard || CraftBookPlugin.plugins.getWorldGuard() == null || CraftBookPlugin.plugins.getWorldGuard().createProtectionQuery().testBlockInteract(player, block);
    }

    /**
     * Checks to see if a block can form at a specific location. This will
     * return true if region protection is disabled or WorldGuard is not found.
     * 
     * @param block The block that is changing.
     * @param newState The new state of the block.
     * 
     * @return Whether the block can form.
     */
    public static boolean canBlockForm(Block block, BlockState newState) {

        if (!shouldUseProtection()) return true;
        if (CraftBookPlugin.inst().getConfiguration().advancedBlockChecks) {

            BlockFormEvent event = new BlockFormEvent(block, newState);
            EventUtil.ignoreEvent(event);
            CraftBookPlugin.inst().getServer().getPluginManager().callEvent(event);
            return !event.isCancelled();
        }
        return !CraftBookPlugin.inst().getConfiguration().obeyWorldguard || CraftBookPlugin.plugins.getWorldGuard() == null || !(newState.getType() == Material.SNOW || newState.getType() == Material.ICE) || CraftBookPlugin.plugins.getWorldGuard().createProtectionQuery().testBlockPlace(null, block.getLocation(), newState.getType());

    }

    /**
     * Checks whether or not protection related code should even be tested.
     * 
     * @return should check or not.
     */
    public static boolean shouldUseProtection() {

        return CraftBookPlugin.inst().getConfiguration().advancedBlockChecks || CraftBookPlugin.inst().getConfiguration().obeyWorldguard;
    }
}