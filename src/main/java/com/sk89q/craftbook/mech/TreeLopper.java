package com.sk89q.craftbook.mech;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.TreeSpecies;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Tree;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.util.BlockUtil;
import com.sk89q.craftbook.util.EventUtil;
import com.sk89q.craftbook.util.ItemInfo;
import com.sk89q.craftbook.util.ItemUtil;
import com.sk89q.craftbook.util.LocationUtil;
import com.sk89q.craftbook.util.ProtectionUtil;

public class TreeLopper extends AbstractCraftBookMechanic {

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {

        if(event.getPlayer().getGameMode() == GameMode.CREATIVE)
            return;

        LocalPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());

        if(!CraftBookPlugin.inst().getConfiguration().treeLopperBlocks.contains(new ItemInfo(event.getBlock()))) return;
        if(!CraftBookPlugin.inst().getConfiguration().treeLopperItems.contains(player.getHeldItemInfo())) return;
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
        if(CraftBookPlugin.inst().getConfiguration().treeLopperPlaceSapling && usedBlock.getState().getData() instanceof Tree && (usedBlock.getRelative(0, -1, 0).getType() == Material.DIRT || usedBlock.getRelative(0, -1, 0).getType() == Material.GRASS || usedBlock.getRelative(0, -1, 0).getType() == Material.MYCEL) && !hasPlanted)
            species = ((Tree) usedBlock.getState().getData()).getSpecies();
        usedBlock.breakNaturally(event.getPlayer().getItemInHand());
        if(species != null) {
            final TreeSpecies fspecies = species;
            Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), new Runnable() {

                @Override
                public void run () {
                    usedBlock.setType(Material.SAPLING);
                    ((Tree) usedBlock.getState().getData()).setSpecies(fspecies);
                }

            }, 2);
            hasPlanted = true;
        }

        for(Block block : CraftBookPlugin.inst().getConfiguration().treeLopperAllowDiagonals ? BlockUtil.getTouchingBlocks(usedBlock) : BlockUtil.getIndirectlyTouchingBlocks(usedBlock)) {
            if(block == null) continue; //Top of map, etc.
            if(visitedLocations.contains(block.getLocation())) continue;
            if(canBreakBlock(event.getPlayer(), originalBlock, block))
                if(searchBlock(event, block, player, originalBlock, visitedLocations, broken, hasPlanted)) {
                    ItemStack heldItem = event.getPlayer().getItemInHand();
                    if(heldItem != null && ItemUtil.getMaxDurability(heldItem.getType()) > 0) {
                        heldItem.setDurability((short) (heldItem.getDurability() + 1));
                        if(heldItem.getDurability() <= ItemUtil.getMaxDurability(heldItem.getType()))
                            event.getPlayer().setItemInHand(heldItem);
                        else
                            event.getPlayer().setItemInHand(null);
                    }
                }
        }
    }

    public boolean canBreakBlock(Player player, ItemInfo originalBlock, Block toBreak) {

        if((originalBlock.getType() == Material.LOG || originalBlock.getType() == Material.LOG_2) && (toBreak.getType() == Material.LEAVES || toBreak.getType() == Material.LEAVES_2) && CraftBookPlugin.inst().getConfiguration().treeLopperBreakLeaves) {
            MaterialData nw = toBreak.getState().getData();
            if(CraftBookPlugin.inst().getConfiguration().treeLopperEnforceData && (!(nw instanceof Tree) || ((Tree) nw).getSpecies() != ((Tree) originalBlock.getMaterialData()).getSpecies()))
                return false;
        }

        if(toBreak.getType() != originalBlock.getType()) return false;
        if(CraftBookPlugin.inst().getConfiguration().treeLopperEnforceData && toBreak.getData() != originalBlock.getData()) return false;

        if(!ProtectionUtil.canBuild(player, toBreak, false)) {
            CraftBookPlugin.inst().wrapPlayer(player).printError("area.break-permissions");
            return false;
        }

        return true;
    }

    public boolean searchBlock(BlockBreakEvent event, Block block, LocalPlayer player, ItemInfo originalBlock, Set<Location> visitedLocations, int broken, boolean hasPlanted) {

        if(visitedLocations.contains(block.getLocation()))
            return false;
        if(broken > CraftBookPlugin.inst().getConfiguration().treeLopperMaxSize)
            return false;
        if(!CraftBookPlugin.inst().getConfiguration().treeLopperItems.contains(player.getHeldItemInfo()))
            return false;
        TreeSpecies species = null;
        if(CraftBookPlugin.inst().getConfiguration().treeLopperPlaceSapling && (block.getRelative(0, -1, 0).getType() == Material.DIRT || block.getRelative(0, -1, 0).getType() == Material.GRASS || block.getRelative(0, -1, 0).getType() == Material.MYCEL) && !hasPlanted)
            species = ((Tree) block.getState().getData()).getSpecies();
        block.breakNaturally(event.getPlayer().getItemInHand());
        if(species != null) {
            block.setType(Material.SAPLING);
            ((Tree) block.getState().getData()).setSpecies(species);
            hasPlanted = true;
        }
        visitedLocations.add(block.getLocation());
        broken += 1;
        for(BlockFace face : CraftBookPlugin.inst().getConfiguration().treeLopperAllowDiagonals ? LocationUtil.getIndirectFaces() : LocationUtil.getDirectFaces()) {
            if(visitedLocations.contains(block.getRelative(face).getLocation())) continue;
            if(canBreakBlock(event.getPlayer(), originalBlock, block.getRelative(face)))
                if(searchBlock(event, block.getRelative(face), player, originalBlock, visitedLocations, broken, hasPlanted)) {
                    ItemStack heldItem = event.getPlayer().getItemInHand();
                    if(heldItem != null && ItemUtil.getMaxDurability(heldItem.getType()) > 0) {
                        heldItem.setDurability((short) (heldItem.getDurability() + 1));
                        if(heldItem.getDurability() <= ItemUtil.getMaxDurability(heldItem.getType()))
                            event.getPlayer().setItemInHand(heldItem);
                        else
                            event.getPlayer().setItemInHand(null);
                    }
                }
        }

        return true;
    }
}