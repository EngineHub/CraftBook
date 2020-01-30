package com.sk89q.craftbook.mechanics;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.CraftBookPlayer;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.util.BlockSyntax;
import com.sk89q.craftbook.util.BlockUtil;
import com.sk89q.craftbook.util.EventUtil;
import com.sk89q.craftbook.util.ItemSyntax;
import com.sk89q.craftbook.util.ItemUtil;
import com.sk89q.craftbook.util.LocationUtil;
import com.sk89q.craftbook.util.ProtectionUtil;
import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.blocks.Blocks;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.HandSide;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockCategories;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.item.ItemType;
import com.sk89q.worldedit.world.item.ItemTypes;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.TreeSpecies;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Leaves;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Tree;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class TreeLopper extends AbstractCraftBookMechanic {

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {

        if(event.getPlayer().getGameMode() == GameMode.CREATIVE)
            return;

        CraftBookPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());

        if(!Blocks.containsFuzzy(enabledBlocks, BukkitAdapter.adapt(event.getBlock().getBlockData()))) return;
        if(!enabledItems.contains(player.getItemInHand(HandSide.MAIN_HAND).getType())) return;
        if(!player.hasPermission("craftbook.mech.treelopper.use")) {
            if(CraftBookPlugin.inst().getConfiguration().showPermissionMessages)
                player.printError("mech.use-permission");
            return;
        }

        if(!EventUtil.passesFilter(event))
            return;

        Set<Location> visitedLocations = new HashSet<>();
        visitedLocations.add(event.getBlock().getLocation());
        int broken = 1;

        final Block usedBlock = event.getBlock();

        BlockStateHolder originalBlock = BukkitAdapter.adapt(usedBlock.getBlockData());
        int planted = 0;

        if(!player.hasPermission("craftbook.mech.treelopper.sapling"))
            planted = 100;

        TreeSpecies species = null;
        if(placeSaplings && usedBlock.getState().getData() instanceof Tree
                && (usedBlock.getRelative(0, -1, 0).getType() == Material.DIRT || usedBlock.getRelative(0, -1, 0).getType() == Material.GRASS_BLOCK || usedBlock.getRelative(0, -1, 0).getType() == Material.MYCELIUM)) {
            species = ((Tree) usedBlock.getState().getData()).getSpecies();
        }

        if(species != null && planted < maxSaplings(species)) {
            Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), new SaplingPlanter(usedBlock, species), 2);
            planted ++;
        }

        for(Block block : allowDiagonals ? BlockUtil.getIndirectlyTouchingBlocks(usedBlock) : BlockUtil.getTouchingBlocks(usedBlock)) {
            if(block == null) continue; //Top of map, etc.
            if(visitedLocations.contains(block.getLocation())) continue;
            Material blockMaterial = block.getType();
            if(canBreakBlock(event.getPlayer(), originalBlock, block))
                if(searchBlock(event, block, player, originalBlock, visitedLocations, broken, planted)) {
                    if (!singleDamageAxe && (leavesDamageAxe || !Tag.LEAVES.isTagged(blockMaterial))) {
                        ItemUtil.damageHeldItem(event.getPlayer());
                    }
                }
        }
    }

    private static int maxSaplings(TreeSpecies tree) {
        if (tree == TreeSpecies.DARK_OAK || tree == TreeSpecies.JUNGLE)
            return 4;
        else
            return 1;
    }

    private boolean canBreakBlock(Player player, BlockStateHolder originalBlock, Block toBreak) {

        if(BlockCategories.LOGS.contains(originalBlock) && Tag.LEAVES.isTagged(toBreak.getType()) && breakLeaves) {
//           TODO MaterialData nw = toBreak.getState().getData();
//            Tree old = new Tree(originalBlock.getType(), (byte) originalBlock.getData());
//            if(!(nw instanceof Leaves)) return false;
//            if(((Leaves) nw).getSpecies() != old.getSpecies()) return false;
        } else {
            if(!originalBlock.equalsFuzzy(BukkitAdapter.adapt(toBreak.getBlockData()))) return false;
        }

        if(!ProtectionUtil.canBuild(player, toBreak, false)) {
            CraftBookPlugin.inst().wrapPlayer(player).printError("area.break-permissions");
            return false;
        }

        return true;
    }

    private boolean searchBlock(BlockBreakEvent event, Block block, CraftBookPlayer player, BlockStateHolder originalBlock, Set<Location> visitedLocations, int broken, int planted) {

        if(visitedLocations.contains(block.getLocation()))
            return false;
        if(broken > maxSearchSize)
            return false;
        if(!enabledItems.contains(player.getItemInHand(HandSide.MAIN_HAND).getType()))
            return false;
        TreeSpecies species = null;
        if(placeSaplings
                && (block.getRelative(0, -1, 0).getType() == Material.DIRT || block.getRelative(0, -1, 0).getType() == Material.GRASS_BLOCK || block.getRelative(0, -1, 0).getType() == Material.MYCELIUM)) {
            MaterialData data = block.getState().getData();
            if (data instanceof Leaves)
                species = ((Leaves) data).getSpecies();
            else if (data instanceof Tree)
                species = ((Tree) data).getSpecies();
        }
        block.breakNaturally(event.getPlayer().getItemInHand());
        if(species != null && planted < maxSaplings(species)) {
            Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), new SaplingPlanter(block, species), 2);
            planted ++;
        }
        visitedLocations.add(block.getLocation());
        broken += 1;
        for(BlockFace face : allowDiagonals ? LocationUtil.getIndirectFaces() : LocationUtil.getDirectFaces()) {
            Block relativeBlock = block.getRelative(face);
            Material relativeMaterial = relativeBlock.getType();
            if(visitedLocations.contains(relativeBlock.getLocation())) continue;
            if(canBreakBlock(event.getPlayer(), originalBlock, relativeBlock))
                if(searchBlock(event, relativeBlock, player, originalBlock, visitedLocations, broken, planted)) {
                    if (!singleDamageAxe && (leavesDamageAxe || !Tag.LEAVES.isTagged(relativeMaterial))) {
                        ItemUtil.damageHeldItem(event.getPlayer());
                    }
                }
        }

        return true;
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
    public void loadConfiguration (YAMLProcessor config, String path) {

        config.setComment(path + "block-list", "A list of log blocks. This can be modified to include more logs. (for mod support etc)");
        enabledBlocks = BlockSyntax.getBlocks(config.getStringList(path + "block-list", BlockCategories.LOGS.getAll().stream().map(BlockType::getId).sorted(String::compareToIgnoreCase).collect(Collectors.toList())), true);

        config.setComment(path + "tool-list", "A list of tools that can trigger the TreeLopper mechanic.");
        enabledItems = config.getStringList(path + "tool-list", Arrays.asList(ItemTypes.IRON_AXE.getId(), ItemTypes.WOODEN_AXE.getId(),
                ItemTypes.STONE_AXE.getId(), ItemTypes.DIAMOND_AXE.getId(), ItemTypes.GOLDEN_AXE.getId()))
                .stream().map(ItemSyntax::getItem).map(ItemStack::getType).map(BukkitAdapter::asItemType).collect(Collectors.toList());

        config.setComment(path + "max-size", "The maximum amount of blocks the TreeLopper can break.");
        maxSearchSize = config.getInt(path + "max-size", 30);

        config.setComment(path + "allow-diagonals", "Allow the TreeLopper to break blocks that are diagonal from each other.");
        allowDiagonals = config.getBoolean(path + "allow-diagonals", false);

        config.setComment(path + "place-saplings", "If enabled, TreeLopper will plant a sapling automatically when a tree is broken.");
        placeSaplings = config.getBoolean(path + "place-saplings", false);

        config.setComment(path + "break-leaves", "If enabled, TreeLopper will break leaves connected to the tree. (If enforce-data is enabled, will only break leaves of same type)");
        breakLeaves = config.getBoolean(path + "break-leaves", false);

        config.setComment(path + "single-damage-axe", "Only remove one damage from the axe, regardless of the amount of logs removed.");
        singleDamageAxe = config.getBoolean(path + "single-damage-axe", false);

        config.setComment(path + "leaves-damage-axe", "Whether the leaves will also damage the axe when single-damage-axe is false and break-leaves is true.");
        leavesDamageAxe = config.getBoolean(path + "leaves-damage-axe", true);
    }

    private static class SaplingPlanter implements Runnable {
        private final Block usedBlock;
        private final TreeSpecies fspecies;

        SaplingPlanter(Block usedBlock, TreeSpecies fspecies) {
            this.usedBlock = usedBlock;
            this.fspecies = fspecies;
        }

        @Override
        public void run () {
            Material saplingMaterial;
            switch (fspecies) {
                case DARK_OAK:
                    saplingMaterial = Material.DARK_OAK_SAPLING;
                    break;
                case GENERIC:
                    saplingMaterial = Material.OAK_SAPLING;
                    break;
                case REDWOOD:
                    saplingMaterial = Material.SPRUCE_SAPLING;
                    break;
                case BIRCH:
                    saplingMaterial = Material.BIRCH_SAPLING;
                    break;
                case JUNGLE:
                    saplingMaterial = Material.JUNGLE_SAPLING;
                    break;
                case ACACIA:
                    saplingMaterial = Material.ACACIA_SAPLING;
                    break;
                default:
                    saplingMaterial = Material.OAK_SAPLING;
                    break;
            }
            usedBlock.setType(saplingMaterial);
        }

    }
}
