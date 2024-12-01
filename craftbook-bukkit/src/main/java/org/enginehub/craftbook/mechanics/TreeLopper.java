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

package org.enginehub.craftbook.mechanics;

import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.blocks.BaseItem;
import com.sk89q.worldedit.blocks.Blocks;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.HandSide;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockCategories;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.sk89q.worldedit.world.item.ItemCategories;
import com.sk89q.worldedit.world.item.ItemType;
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
import org.bukkit.event.block.BlockBreakEvent;
import org.enginehub.craftbook.AbstractCraftBookMechanic;
import org.enginehub.craftbook.CraftBook;
import org.enginehub.craftbook.CraftBookPlayer;
import org.enginehub.craftbook.bukkit.CraftBookPlugin;
import org.enginehub.craftbook.mechanic.CraftBookMechanic;
import org.enginehub.craftbook.mechanic.MechanicType;
import org.enginehub.craftbook.mechanic.exception.MechanicInitializationException;
import org.enginehub.craftbook.util.BlockParser;
import org.enginehub.craftbook.util.BlockUtil;
import org.enginehub.craftbook.util.ConfigUtil;
import org.enginehub.craftbook.util.EventUtil;
import org.enginehub.craftbook.util.ItemParser;
import org.enginehub.craftbook.util.ProtectionUtil;
import org.enginehub.craftbook.util.TernaryState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class TreeLopper extends AbstractCraftBookMechanic {

    private final Map<Material, Material> logsToSaplings = new HashMap<>();
    private final Map<Material, Material> leavesToSaplings = new HashMap<>();
    private final Map<Material, Material> logsToLeaves = new HashMap<>();

    public TreeLopper(MechanicType<? extends CraftBookMechanic> mechanicType) {
        super(mechanicType);
    }

    @Override
    public void enable() throws MechanicInitializationException {
        super.enable();

        logsToSaplings.clear();
        leavesToSaplings.clear();

        // A few special cases
        logsToLeaves.put(Material.CRIMSON_STEM, Material.NETHER_WART_BLOCK);
        logsToSaplings.put(Material.CRIMSON_STEM, Material.CRIMSON_FUNGUS);
        leavesToSaplings.put(Material.NETHER_WART_BLOCK, Material.CRIMSON_FUNGUS);

        logsToLeaves.put(Material.WARPED_STEM, Material.WARPED_WART_BLOCK);
        logsToSaplings.put(Material.WARPED_STEM, Material.WARPED_FUNGUS);
        leavesToSaplings.put(Material.WARPED_WART_BLOCK, Material.WARPED_FUNGUS);

        logsToSaplings.put(Material.MANGROVE_LOG, Material.MANGROVE_PROPAGULE);
        leavesToSaplings.put(Material.MANGROVE_LEAVES, Material.MANGROVE_PROPAGULE);

        Tag.PLANKS.getValues().stream().map(Material::getKey).map(NamespacedKey::asString).forEach(key -> {
            if (key.endsWith("crimson_planks") || key.endsWith("warped_planks")) {
                return;
            }
            Material sapling = Material.matchMaterial(key.replace("planks", "sapling"));
            Material leaves = Material.matchMaterial(key.replace("planks", "leaves"));
            Material log = Material.matchMaterial(key.replace("planks", "log"));

            if (leaves == null || log == null) {
                CraftBook.LOGGER.debug("Failed to find leaves, or log for " + key);
                return;
            }

            logsToLeaves.put(log, leaves);

            if (sapling == null && !logsToSaplings.containsKey(log)) {
                CraftBook.LOGGER.debug("Failed to find sapling for " + key);
                return;
            }

            logsToSaplings.put(log, sapling);
            leavesToSaplings.put(leaves, sapling);
        });
    }

    @Override
    public void disable() {
        super.disable();

        logsToSaplings.clear();
        leavesToSaplings.clear();
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
            case DARK_OAK_SAPLING, JUNGLE_SAPLING -> 4;
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
            if (breakLeaves && logsToLeaves.containsKey(originalBlock)) {
                if (logsToLeaves.get(originalBlock) != toBreakType) {
                    return false;
                }
            } else {
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
            if (placeSaplings && allowPlanting && planted < Integer.MAX_VALUE) {
                if (leavesToSaplings.containsKey(currentType)) {
                    saplingType = leavesToSaplings.get(currentType);
                } else if (logsToSaplings.containsKey(currentType)) {
                    saplingType = logsToSaplings.get(currentType);
                }
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

            for (Block relativeBlock : allowDiagonals ? BlockUtil.getIndirectlyTouchingBlocks(block) : BlockUtil.getTouchingBlocks(block)) {
                if (visitedLocations.contains(relativeBlock.getLocation())) {
                    continue;
                }

                queue.add(relativeBlock);
            }
        }
    }

    List<BaseBlock> enabledBlocks;
    List<ItemType> enabledItems;
    private int maxSearchSize;
    private boolean allowDiagonals;
    private boolean placeSaplings;
    private boolean breakLeaves;
    private boolean singleDamageAxe;
    private boolean leavesDamageAxe;
    private TernaryState allowSneaking;

    private List<String> getDefaultBlocks() {
        List<String> materials = new ArrayList<>();
        materials.addAll(ConfigUtil.getIdsFromCategory(BlockCategories.OVERWORLD_NATURAL_LOGS));
        materials.add(BlockTypes.CRIMSON_STEM.id());
        materials.add(BlockTypes.WARPED_STEM.id());
        return materials;
    }

    @Override
    public void loadFromConfiguration(YAMLProcessor config) {
        config.setComment("enabled-blocks", "A list of enabled log blocks. This list can only contain logs, but can be modified to include more logs (for mod support).");
        enabledBlocks = BlockParser.getBlocks(config.getStringList("enabled-blocks", getDefaultBlocks().stream().sorted(String::compareToIgnoreCase).toList()), true);

        config.setComment("tool-list", "A list of tools that can trigger the TreeLopper mechanic.");
        enabledItems = ItemParser.getItems(config.getStringList("tool-list", ConfigUtil.getIdsFromCategory(ItemCategories.AXES)), true).stream().map(BaseItem::getType).toList();

        config.setComment("max-size", "The maximum amount of blocks the TreeLopper can break.");
        maxSearchSize = config.getInt("max-size", 75);

        config.setComment("allow-diagonals", "Allow the TreeLopper to break blocks that are diagonal from each other.");
        allowDiagonals = config.getBoolean("allow-diagonals", false);

        config.setComment("place-saplings", "If enabled, TreeLopper will plant a sapling automatically when a tree is broken.");
        placeSaplings = config.getBoolean("place-saplings", false);

        config.setComment("break-leaves", "If enabled, TreeLopper will break leaves connected to the tree.");
        breakLeaves = config.getBoolean("break-leaves", true);

        config.setComment("leaves-damage-axe", "Whether the leaves will also damage the axe when single-damage-axe is false and break-leaves is true.");
        leavesDamageAxe = config.getBoolean("leaves-damage-axe", false);

        config.setComment("single-damage-axe", "Only remove one damage from the axe, regardless of the amount of blocks removed.");
        singleDamageAxe = config.getBoolean("single-damage-axe", false);

        config.setComment("allow-sneaking", "Sets how the player must be sneaking in order to use the Tree Lopper.");
        allowSneaking = TernaryState.parseTernaryState(config.getString("allow-sneaking", TernaryState.NONE.toString()));
    }

    private record SaplingPlanter(Block location, Material sapling) implements Runnable {
        @Override
        public void run() {
            this.location.setType(sapling);
        }
    }
}
