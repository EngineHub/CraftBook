package com.sk89q.craftbook.mechanics;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.CraftBookPlayer;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.util.*;
import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.util.HandSide;
import com.sk89q.worldedit.world.item.ItemType;
import com.sk89q.worldedit.world.item.ItemTypes;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.material.Leaves;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Sapling;
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

        if(!enabledBlocks.contains(new ItemInfo(event.getBlock()))) return;
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

        ItemInfo originalBlock = new ItemInfo(usedBlock);
        int planted = 0;

        if(!player.hasPermission("craftbook.mech.treelopper.sapling"))
            planted = 100;

        TreeSpecies species = null;
        if(placeSaplings && usedBlock.getState().getData() instanceof Tree
                && (usedBlock.getRelative(0, -1, 0).getType() == Material.DIRT || usedBlock.getRelative(0, -1, 0).getType() == Material.GRASS_BLOCK || usedBlock.getRelative(0, -1, 0).getType() == Material.MYCELIUM))
            species = ((Tree) usedBlock.getState().getData()).getSpecies();
        usedBlock.breakNaturally(event.getPlayer().getInventory().getItemInMainHand());
        if(species != null && planted < maxSaplings(species)) {
            Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), new SaplingPlanter(usedBlock, species), 2);
            planted ++;
        }

        for(Block block : allowDiagonals ? BlockUtil.getIndirectlyTouchingBlocks(usedBlock) : BlockUtil.getTouchingBlocks(usedBlock)) {
            if(block == null) continue; //Top of map, etc.
            if(visitedLocations.contains(block.getLocation())) continue;
            if(canBreakBlock(event.getPlayer(), originalBlock, block))
                if(searchBlock(event, block, player, originalBlock, visitedLocations, broken, planted)) {
                    if (!singleDamageAxe) {
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

    private boolean canBreakBlock(Player player, ItemInfo originalBlock, Block toBreak) {

        if(Tag.LOGS.isTagged(originalBlock.getType()) && Tag.LEAVES.isTagged(toBreak.getType()) && breakLeaves) {
            MaterialData nw = toBreak.getState().getData();
            Tree old = new Tree(originalBlock.getType(), (byte) originalBlock.getData());
            if(!(nw instanceof Leaves)) return false;
            if(((Leaves) nw).getSpecies() != old.getSpecies()) return false;
        } else {
            if(toBreak.getType() != originalBlock.getType()) return false;
        }

        if(!ProtectionUtil.canBuild(player, toBreak, false)) {
            CraftBookPlugin.inst().wrapPlayer(player).printError("area.break-permissions");
            return false;
        }

        return true;
    }

    private boolean searchBlock(BlockBreakEvent event, Block block, CraftBookPlayer player, ItemInfo originalBlock, Set<Location> visitedLocations, int broken, int planted) {

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
            if(visitedLocations.contains(block.getRelative(face).getLocation())) continue;
            if(canBreakBlock(event.getPlayer(), originalBlock, block.getRelative(face)))
                if(searchBlock(event, block.getRelative(face), player, originalBlock, visitedLocations, broken, planted)) {
                    if (!singleDamageAxe) {
                        ItemUtil.damageHeldItem(event.getPlayer());
                    }
                }
        }

        return true;
    }

    List<ItemInfo> enabledBlocks;
    List<ItemType> enabledItems;
    private int maxSearchSize;
    private boolean allowDiagonals;
    private boolean placeSaplings;
    private boolean breakLeaves;
    private boolean singleDamageAxe;

    @Override
    public void loadConfiguration (YAMLProcessor config, String path) {

        config.setComment(path + "block-list", "A list of log blocks. This can be modified to include more logs. (for mod support etc)");
        enabledBlocks = ItemInfo.parseListFromString(config.getStringList(path + "block-list", Arrays.asList("LOG", "LOG_2")));

        config.setComment(path + "tool-list", "A list of tools that can trigger the TreeLopper mechanic.");
        enabledItems = config.getStringList(path + "tool-list", Arrays.asList(ItemTypes.IRON_AXE.getId(), ItemTypes.WOODEN_AXE.getId(),
                ItemTypes.STONE_AXE.getId(), ItemTypes.DIAMOND_AXE.getId(), ItemTypes.GOLDEN_AXE.getId())).stream().map(ItemTypes::get).collect(Collectors.toList());

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
            usedBlock.setType(Material.SAPLING);
            BlockState state = usedBlock.getState();
            Sapling sapling = (Sapling) state.getData();
            System.out.println(fspecies.name());
            sapling.setSpecies(fspecies);
            state.setData(sapling);
            state.update();
        }

    }
}
