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
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.item.ItemType;
import com.sk89q.worldedit.world.item.ItemTypes;
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
import org.bukkit.inventory.EquipmentSlot;
import org.enginehub.craftbook.AbstractCraftBookMechanic;
import org.enginehub.craftbook.CraftBook;
import org.enginehub.craftbook.CraftBookPlayer;
import org.enginehub.craftbook.bukkit.CraftBookPlugin;
import org.enginehub.craftbook.mechanic.exception.MechanicInitializationException;
import org.enginehub.craftbook.util.BlockParser;
import org.enginehub.craftbook.util.BlockUtil;
import org.enginehub.craftbook.util.EventUtil;
import org.enginehub.craftbook.util.ItemParser;
import org.enginehub.craftbook.util.ProtectionUtil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class TreeLopper extends AbstractCraftBookMechanic {

    private final Map<Material, Material> LOG_TO_SAPLING = new HashMap<>();
    private final Map<Material, Material> LEAVES_TO_SAPLING = new HashMap<>();

    @Override
    public void enable() throws MechanicInitializationException {
        super.enable();

        LOG_TO_SAPLING.clear();
        LEAVES_TO_SAPLING.clear();

        Tag.PLANKS.getValues().stream().map(Material::getKey).map(NamespacedKey::asString).forEach(key -> {
            Material sapling = Material.matchMaterial(key.replace("planks", "sapling"));
            Material leaves = Material.matchMaterial(key.replace("planks", "leaves"));
            Material log = Material.matchMaterial(key.replace("planks", "log"));

            if (sapling == null || leaves == null || log == null) {
                CraftBook.LOGGER.warn("Failed to find sapling, leaves, or log for " + key);
                return;
            }

            LOG_TO_SAPLING.put(log, sapling);
            LEAVES_TO_SAPLING.put(leaves, sapling);
        });
    }

    @Override
    public void disable() {
        super.disable();

        LOG_TO_SAPLING.clear();
        LEAVES_TO_SAPLING.clear();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getPlayer().getGameMode() == GameMode.CREATIVE || !EventUtil.passesFilter(event)) {
            return;
        }

        CraftBookPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());

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

        Set<Location> visitedLocations = new HashSet<>();
        Block usedBlock = event.getBlock();
        Material originalBlock = usedBlock.getType();

        // Set the planted value very high if we're not allowed to plant saplings
        int planted = player.hasPermission("craftbook.treelopper.sapling") ? 0 : Integer.MAX_VALUE;

        searchBlock(event.getPlayer(), usedBlock, originalBlock, visitedLocations, new AtomicInteger(0), planted);
    }

    private static int getMaximumSaplingCount(Material tree) {
        return switch (tree) {
            case DARK_OAK_SAPLING, JUNGLE_SAPLING -> 4;
            default -> 1;
        };
    }

    private boolean canBreakBlock(Player player, Material originalBlock, Block toBreak) {
        Material toBreakType = toBreak.getType();

        if (breakLeaves && LOG_TO_SAPLING.containsKey(originalBlock) && LEAVES_TO_SAPLING.containsKey(toBreakType)) {
           if (LOG_TO_SAPLING.get(originalBlock) != LEAVES_TO_SAPLING.get(toBreakType)) {
               return false;
           }
        } else if (originalBlock != toBreakType) {
            return false;
        }

        if (ProtectionUtil.isBreakingPrevented(player, toBreak)) {
            CraftBookPlugin.inst().wrapPlayer(player).printError(TranslatableComponent.of("craftbook.mechanisms.protection-blocked", TextComponent.of(getMechanicType().getName())));
            return false;
        }

        return true;
    }

    private void searchBlock(Player player, Block block, Material baseType, Set<Location> visitedLocations, AtomicInteger broken, int planted) {
        if (broken.get() > maxSearchSize || visitedLocations.contains(block.getLocation()) || !canBreakBlock(player, baseType, block)) {
            return;
        }

        Material currentType = block.getType();
        Material belowBlockType = block.getRelative(0, -1, 0).getType();

        Material saplingType = null;
        if (placeSaplings && planted < Integer.MAX_VALUE && Tag.DIRT.isTagged(belowBlockType)) {
            if (LEAVES_TO_SAPLING.containsKey(currentType)) {
                saplingType = LEAVES_TO_SAPLING.get(currentType);
            } else if (LOG_TO_SAPLING.containsKey(currentType)) {
                saplingType = LOG_TO_SAPLING.get(currentType);
            }
        }

        if (saplingType != null && planted < getMaximumSaplingCount(saplingType)) {
            Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), new SaplingPlanter(block, saplingType), 2);
            planted++;
        }

        block.breakNaturally(player.getInventory().getItem(EquipmentSlot.HAND));
        if (!singleDamageAxe && (leavesDamageAxe || !Tag.LEAVES.isTagged(currentType))) {
            if (player.getInventory().getItemInMainHand().damage(1, player).getAmount() == 0) {
                // We broke the axe, so we can't continue
                return;
            }
        }

        visitedLocations.add(block.getLocation());
        broken.incrementAndGet();

        for (Block relativeBlock : allowDiagonals ? BlockUtil.getIndirectlyTouchingBlocks(block) : BlockUtil.getTouchingBlocks(block)) {
            if (visitedLocations.contains(relativeBlock.getLocation())) {
                continue;
            }

            searchBlock(player, relativeBlock, baseType, visitedLocations, broken, planted);
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

    @Override
    public void loadFromConfiguration(YAMLProcessor config) {
        config.setComment("enabled-blocks", "A list of enabled log blocks. This list can only contain logs, but can be modified to include more logs (for mod support).");
        enabledBlocks = BlockParser.getBlocks(config.getStringList("enabled-blocks", BlockCategories.LOGS.getAll().stream().map(BlockType::getId).sorted(String::compareToIgnoreCase).toList()), true);

        config.setComment("tool-list", "A list of tools that can trigger the TreeLopper mechanic.");
        enabledItems = ItemParser.getItems(config.getStringList("tool-list", Arrays.asList(ItemTypes.IRON_AXE.getId(), ItemTypes.WOODEN_AXE.getId(),
            ItemTypes.STONE_AXE.getId(), ItemTypes.DIAMOND_AXE.getId(), ItemTypes.GOLDEN_AXE.getId(), ItemTypes.NETHERITE_AXE.getId())), true).stream().map(BaseItem::getType).toList();

        config.setComment("max-size", "The maximum amount of blocks the TreeLopper can break.");
        maxSearchSize = config.getInt("max-size", 30);

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
    }

    private record SaplingPlanter(Block location, Material sapling) implements Runnable {
        @Override
        public void run() {
            this.location.setType(sapling);
        }
    }
}
