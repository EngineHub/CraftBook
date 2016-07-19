package com.sk89q.craftbook.mechanics;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.util.*;
import com.sk89q.util.yaml.YAMLProcessor;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
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

public class TreeLopper extends AbstractCraftBookMechanic {

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {

        if(event.getPlayer().getGameMode() == GameMode.CREATIVE)
            return;

        LocalPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());

        if(!enabledBlocks.contains(new ItemInfo(event.getBlock()))) return;
        if(!enabledItems.contains(player.getHeldItemInfo())) return;
        if(!player.hasPermission("craftbook.mech.treelopper.use")) {
            if(CraftBookPlugin.inst().getConfiguration().showPermissionMessages)
                player.printError("mech.use-permission");
            return;
        }

        if(!EventUtil.passesFilter(event))
            return;

        Set<Location> visitedLocations = new HashSet<Location>();
        visitedLocations.add(event.getBlock().getLocation());
        int broken = 1;

        final Block usedBlock = event.getBlock();

        ItemInfo originalBlock = new ItemInfo(usedBlock);
        boolean hasPlanted = false;

        if(!player.hasPermission("craftbook.mech.treelopper.sapling"))
            hasPlanted = true;

        TreeSpecies species = null;
        if(placeSaplings && usedBlock.getState().getData() instanceof Tree && (usedBlock.getRelative(0, -1, 0).getType() == Material.DIRT || usedBlock.getRelative(0, -1, 0).getType() == Material.GRASS || usedBlock.getRelative(0, -1, 0).getType() == Material.MYCEL) && !hasPlanted)
            species = ((Tree) usedBlock.getState().getData()).getSpecies();
        usedBlock.breakNaturally(event.getPlayer().getItemInHand());
        if(species != null) {
            final TreeSpecies fspecies = species;
            Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), new Runnable() {

                @Override
                public void run () {
                    usedBlock.setType(Material.SAPLING);
                    Sapling sapling = (Sapling) usedBlock.getState().getData();
                    sapling.setSpecies(fspecies);
                    usedBlock.getState().setData(sapling);
                }

            }, 2);
            hasPlanted = true;
        }

        for(Block block : allowDiagonals ? BlockUtil.getIndirectlyTouchingBlocks(usedBlock) : BlockUtil.getTouchingBlocks(usedBlock)) {
            if(block == null) continue; //Top of map, etc.
            if(visitedLocations.contains(block.getLocation())) continue;
            if(canBreakBlock(event.getPlayer(), originalBlock, block))
                if(searchBlock(event, block, player, originalBlock, visitedLocations, broken, hasPlanted)) {
                    ItemUtil.damageHeldItem(event.getPlayer());
                }
        }
    }

    public boolean canBreakBlock(Player player, ItemInfo originalBlock, Block toBreak) {

        if((originalBlock.getType() == Material.LOG || originalBlock.getType() == Material.LOG_2) && (toBreak.getType() == Material.LEAVES || toBreak.getType() == Material.LEAVES_2) && breakLeaves) {
            MaterialData nw = toBreak.getState().getData();
            Tree old = new Tree(originalBlock.getType(), (byte) originalBlock.getData());
            if(!(nw instanceof Leaves)) return false;
            if(enforceDataValues && ((Leaves) nw).getSpecies() != old.getSpecies()) return false;
        } else {
            if(toBreak.getType() != originalBlock.getType()) return false;
            if(enforceDataValues && toBreak.getData() != originalBlock.getData()) return false;
        }

        if(!ProtectionUtil.canBuild(player, toBreak, false)) {
            CraftBookPlugin.inst().wrapPlayer(player).printError("area.break-permissions");
            return false;
        }

        return true;
    }

    public boolean searchBlock(BlockBreakEvent event, Block block, LocalPlayer player, ItemInfo originalBlock, Set<Location> visitedLocations, int broken, boolean hasPlanted) {

        if(visitedLocations.contains(block.getLocation()))
            return false;
        if(broken > maxSearchSize)
            return false;
        if(!enabledItems.contains(player.getHeldItemInfo()))
            return false;
        TreeSpecies species = null;
        if(placeSaplings && (block.getRelative(0, -1, 0).getType() == Material.DIRT || block.getRelative(0, -1, 0).getType() == Material.GRASS || block.getRelative(0, -1, 0).getType() == Material.MYCEL) && !hasPlanted) {
            MaterialData data = block.getState().getData();
            if (data instanceof Leaves)
                species = ((Leaves) data).getSpecies();
            else if (data instanceof Tree)
                species = ((Tree) data).getSpecies();
        }
        block.breakNaturally(event.getPlayer().getItemInHand());
        if(species != null) {
            block.setType(Material.SAPLING);
            Sapling sapling = (Sapling) block.getState().getData();
            sapling.setSpecies(species);
            block.getState().setData(sapling);
            hasPlanted = true;
        }
        visitedLocations.add(block.getLocation());
        broken += 1;
        for(BlockFace face : allowDiagonals ? LocationUtil.getIndirectFaces() : LocationUtil.getDirectFaces()) {
            if(visitedLocations.contains(block.getRelative(face).getLocation())) continue;
            if(canBreakBlock(event.getPlayer(), originalBlock, block.getRelative(face)))
                if(searchBlock(event, block.getRelative(face), player, originalBlock, visitedLocations, broken, hasPlanted)) {
                    ItemUtil.damageHeldItem(event.getPlayer());
                }
        }

        return true;
    }

    List<ItemInfo> enabledBlocks;
    List<ItemInfo> enabledItems;
    private int maxSearchSize;
    private boolean allowDiagonals;
    private boolean enforceDataValues;
    private boolean placeSaplings;
    private boolean breakLeaves;

    @Override
    public void loadConfiguration (YAMLProcessor config, String path) {

        config.setComment(path + "block-list", "A list of log blocks. This can be modified to include more logs. (for mod support etc)");
        enabledBlocks = ItemInfo.parseListFromString(config.getStringList(path + "block-list", Arrays.asList("LOG", "LOG_2")));

        config.setComment(path + "tool-list", "A list of tools that can trigger the TreeLopper mechanic.");
        enabledItems = ItemInfo.parseListFromString(config.getStringList(path + "tool-list", Arrays.asList("IRON_AXE", "WOOD_AXE", "STONE_AXE", "DIAMOND_AXE", "GOLD_AXE")));

        config.setComment(path + "max-size", "The maximum amount of blocks the TreeLopper can break.");
        maxSearchSize = config.getInt(path + "max-size", 30);

        config.setComment(path + "allow-diagonals", "Allow the TreeLopper to break blocks that are diagonal from each other.");
        allowDiagonals = config.getBoolean(path + "allow-diagonals", false);

        config.setComment(path + "enforce-data", "Make sure the blocks broken by TreeLopper all share the same data values.");
        enforceDataValues = config.getBoolean(path + "enforce-data", false);

        config.setComment(path + "place-saplings", "If enabled, TreeLopper will plant a sapling automatically when a tree is broken.");
        placeSaplings = config.getBoolean(path + "place-saplings", false);

        config.setComment(path + "break-leaves", "If enabled, TreeLopper will break leaves connected to the tree. (If enforce-data is enabled, will only break leaves of same type)");
        breakLeaves = config.getBoolean(path + "break-leaves", false);
    }
}
