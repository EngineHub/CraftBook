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

package org.enginehub.craftbook.bukkit.mechanics;

import com.sk89q.worldedit.blocks.Blocks;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.HandSide;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.enginehub.craftbook.CraftBook;
import org.enginehub.craftbook.CraftBookPlayer;
import org.enginehub.craftbook.bukkit.CraftBookPlugin;
import org.enginehub.craftbook.mechanic.CraftBookMechanic;
import org.enginehub.craftbook.mechanic.MechanicType;
import org.enginehub.craftbook.mechanic.exception.MechanicInitializationException;
import org.enginehub.craftbook.mechanics.TreeLopper;
import org.enginehub.craftbook.util.BlockUtil;
import org.enginehub.craftbook.util.EventUtil;
import org.enginehub.craftbook.util.ProtectionUtil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class BukkitTreeLopper extends TreeLopper implements Listener {

    private final Map<Material, Material> blockToSaplings = new HashMap<>();
    private final Map<Material, Material> logsToLeaves = new HashMap<>();
    private final Map<Material, Material> logsToRoots = new HashMap<>();

    public BukkitTreeLopper(MechanicType<? extends CraftBookMechanic> mechanicType) {
        super(mechanicType);
    }

    @Override
    public void enable() throws MechanicInitializationException {
        super.enable();

        blockToSaplings.clear();
        logsToLeaves.clear();

        // A few special cases
        logsToLeaves.put(Material.CRIMSON_STEM, Material.NETHER_WART_BLOCK);
        blockToSaplings.put(Material.CRIMSON_STEM, Material.CRIMSON_FUNGUS);
        blockToSaplings.put(Material.NETHER_WART_BLOCK, Material.CRIMSON_FUNGUS);

        logsToLeaves.put(Material.WARPED_STEM, Material.WARPED_WART_BLOCK);
        blockToSaplings.put(Material.WARPED_STEM, Material.WARPED_FUNGUS);
        blockToSaplings.put(Material.WARPED_WART_BLOCK, Material.WARPED_FUNGUS);

        blockToSaplings.put(Material.MANGROVE_LOG, Material.MANGROVE_PROPAGULE);
        blockToSaplings.put(Material.MANGROVE_LEAVES, Material.MANGROVE_PROPAGULE);

        Tag.PLANKS.getValues().stream().map(Material::getKey).map(NamespacedKey::asString).forEach(key -> {
            if (key.endsWith("crimson_planks") || key.endsWith("warped_planks")) {
                return;
            }
            Material sapling = Material.matchMaterial(key.replace("planks", "sapling"));
            Material leaves = Material.matchMaterial(key.replace("planks", "leaves"));
            Material log = Material.matchMaterial(key.replace("planks", "log"));
            Material roots = Material.matchMaterial(key.replace("planks", "roots"));

            if (leaves == null && log == null && roots == null) {
                CraftBook.LOGGER.debug("Failed to find any of leaves, roots, or logs for " + key);
                return;
            }

            if (log != null && leaves != null) {
                logsToLeaves.put(log, leaves);
            }
            if (log != null && roots != null) {
                logsToRoots.put(log, roots);
            }

            if (sapling == null) {
                if (!blockToSaplings.containsKey(leaves) && !blockToSaplings.containsKey(log)) {
                    // Don't log if we already have a sapling for this leaf or log
                    CraftBook.LOGGER.debug("Failed to find sapling for " + key);
                }
                return;
            }

            if (log != null) {
                blockToSaplings.put(log, sapling);
            }
            if (leaves != null) {
                blockToSaplings.put(leaves, sapling);
            }
        });
    }

    @Override
    public void disable() {
        super.disable();

        blockToSaplings.clear();
        logsToLeaves.clear();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getPlayer().getGameMode() == GameMode.CREATIVE || !EventUtil.passesFilter(event)) {
            return;
        }

        CraftBookPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());

        if (!allowSneaking.doesPass(player.isSneaking())) {
            return;
        }

        if (!Blocks.containsFuzzy(enabledBlocks, BukkitAdapter.adapt(event.getBlock().getBlockData()))) {
            return;
        }

        if (!enabledItems.contains(player.getItemInHand(HandSide.MAIN_HAND).getType())) {
            return;
        }

        if (!player.hasPermission("craftbook.treelopper.use")) {
            if (CraftBook.getInstance().getPlatform().getConfiguration().showPermissionMessages) {
                player.printError(TranslatableComponent.of("craftbook.mechanisms.use-permission", TextComponent.of(getMechanicType().getName())));
            }
            return;
        }

        Block usedBlock = event.getBlock();

        boolean allowPlanting = player.hasPermission("craftbook.treelopper.sapling");

        searchBlock(event.getPlayer(), usedBlock, allowPlanting);
    }

    private static int getMaximumSaplingCount(Material tree) {
        return switch (tree) {
            case DARK_OAK_SAPLING, JUNGLE_SAPLING, PALE_OAK_SAPLING -> 4;
            default -> 1;
        };
    }

    private static boolean canPlaceOn(Material saplingType, Material blockType) {
        return switch (saplingType) {
            case WARPED_FUNGUS, CRIMSON_FUNGUS -> blockType == Material.CRIMSON_NYLIUM || blockType == Material.WARPED_NYLIUM || Tag.DIRT.isTagged(blockType);
            default -> Tag.DIRT.isTagged(blockType);
        };
    }

    private boolean canBreakBlock(Player player, Material originalBlock, Block toBreak) {
        Material toBreakType = toBreak.getType();

        if (originalBlock != toBreakType) {
            boolean canBreak = false;

            if (breakLeaves && logsToLeaves.containsKey(originalBlock)) {
                if (logsToLeaves.get(originalBlock) == toBreakType) {
                    canBreak = true;
                }
            }
            if (!canBreak && breakRoots && logsToRoots.containsKey(originalBlock)) {
                if (logsToRoots.get(originalBlock) == toBreakType) {
                    canBreak = true;
                }
            }

            if (!canBreak) {
                return false;
            }
        }

        if (!ProtectionUtil.canBreak(player, toBreak)) {
            CraftBookPlugin.inst().wrapPlayer(player).printError(TranslatableComponent.of("craftbook.mechanisms.protection-blocked", TextComponent.of(getMechanicType().getName())));
            return false;
        }

        return true;
    }

    private void searchBlock(Player player, Block baseBlock, boolean allowPlanting) {
        Queue<Block> queue = new LinkedList<>();
        Set<Location> visitedLocations = new HashSet<>();
        int broken = 0;
        int planted = 0;
        Material baseType = baseBlock.getType();

        queue.add(baseBlock);

        while (!queue.isEmpty() && broken < maxSearchSize) {
            Block block = queue.poll();

            if (visitedLocations.contains(block.getLocation()) || !canBreakBlock(player, baseType, block)) {
                continue;
            }

            Material currentType = block.getType();
            Material belowBlockType = block.getRelative(0, -1, 0).getType();

            Material saplingType = null;
            if (placeSaplings && allowPlanting && blockToSaplings.containsKey(currentType)) {
                saplingType = blockToSaplings.get(currentType);
            }

            if (saplingType != null && planted < getMaximumSaplingCount(saplingType) && canPlaceOn(saplingType, belowBlockType)) {
                Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), new SaplingPlanter(block, saplingType), 2);
                planted++;
            }

            var mainHandItem = player.getInventory().getItemInMainHand();
            block.breakNaturally(mainHandItem);
            if (!singleDamageAxe && (leavesDamageAxe || !Tag.LEAVES.isTagged(currentType)) && !mainHandItem.isEmpty()) {
                if (mainHandItem.damage(1, player).isEmpty()) {
                    // We broke the axe, so we can't continue
                    return;
                }
            }

            visitedLocations.add(block.getLocation());
            broken++;

            var searchBlocks = allowDiagonals ? BlockUtil.getIndirectlyTouchingBlocks(block) : BlockUtil.getTouchingBlocks(block);

            for (Block relativeBlock : searchBlocks) {
                if (visitedLocations.contains(relativeBlock.getLocation())) {
                    continue;
                }

                queue.add(relativeBlock);
            }
        }
    }

    private record SaplingPlanter(Block location, Material sapling) implements Runnable {
        @Override
        public void run() {
            this.location.setType(sapling);
        }
    }
}
