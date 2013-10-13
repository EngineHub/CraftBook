package com.sk89q.craftbook.mech;

import java.util.HashSet;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.TreeSpecies;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Tree;

import com.sk89q.craftbook.AbstractMechanic;
import com.sk89q.craftbook.AbstractMechanicFactory;
import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.MechanicListenerAdapter;
import com.sk89q.craftbook.util.ItemInfo;
import com.sk89q.craftbook.util.ItemUtil;
import com.sk89q.craftbook.util.LocationUtil;
import com.sk89q.worldedit.BlockWorldVector;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.bukkit.BukkitUtil;

public class TreeLopper extends AbstractMechanic {

    protected final CraftBookPlugin plugin = CraftBookPlugin.inst();

    private int broken;
    Block usedBlock;

    private boolean hasPlanted = false;

    private HashSet<Location> visitedLocations = new HashSet<Location>();

    @Override
    public void onBlockBreak(BlockBreakEvent event) {

        if(MechanicListenerAdapter.shouldIgnoreEvent(event))
            return;
        if(event.getPlayer().getGameMode() == GameMode.CREATIVE)
            return;

        visitedLocations.add(event.getBlock().getLocation());
        broken = 1;

        usedBlock = event.getBlock();

        TreeSpecies species = null;
        if(CraftBookPlugin.inst().getConfiguration().treeLopperPlaceSapling && (usedBlock.getRelative(0, -1, 0).getTypeId() == BlockID.DIRT || usedBlock.getRelative(0, -1, 0).getTypeId() == BlockID.GRASS || usedBlock.getRelative(0, -1, 0).getTypeId() == BlockID.MYCELIUM) && !hasPlanted)
            species = ((Tree) usedBlock.getState().getData()).getSpecies();
        usedBlock.breakNaturally(event.getPlayer().getItemInHand());
        if(species != null) {
            usedBlock.setTypeId(BlockID.SAPLING);
            ((Tree) usedBlock.getState().getData()).setSpecies(species);
            hasPlanted = true;
        }

        for(BlockFace face : plugin.getConfiguration().treeLopperAllowDiagonals ? LocationUtil.getIndirectFaces() : LocationUtil.getDirectFaces()) {
            if(event.getBlock().getRelative(face).getTypeId() == usedBlock.getTypeId() && (!plugin.getConfiguration().treeLopperEnforceData || event.getBlock().getRelative(face).getData() == usedBlock.getData()))
                if(searchBlock(event, usedBlock.getRelative(face))) {
                    ItemStack heldItem = event.getPlayer().getItemInHand();
                    if(heldItem != null && ItemUtil.getMaxDurability(heldItem.getTypeId()) > 0) {
                        heldItem.setDurability((short) (heldItem.getDurability() + 1));
                        if(heldItem.getDurability() <= ItemUtil.getMaxDurability(heldItem.getTypeId()))
                            event.getPlayer().setItemInHand(heldItem);
                        else
                            event.getPlayer().setItemInHand(null);
                    }
                }
        }
    }

    public boolean searchBlock(BlockBreakEvent event, Block block) {

        LocalPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());
        if(visitedLocations.contains(block.getLocation()))
            return false;
        if(broken > plugin.getConfiguration().treeLopperMaxSize)
            return false;
        if(!CraftBookPlugin.inst().getConfiguration().treeLopperItems.contains(player.getHeldItemInfo()))
            return false;
        if(!plugin.canBuild(event.getPlayer(), block, false)) {
            player.printError("area.break-permissions");
            return false;
        }
        TreeSpecies species = null;
        if(CraftBookPlugin.inst().getConfiguration().treeLopperPlaceSapling && (block.getRelative(0, -1, 0).getTypeId() == BlockID.DIRT || block.getRelative(0, -1, 0).getTypeId() == BlockID.GRASS || block.getRelative(0, -1, 0).getTypeId() == BlockID.MYCELIUM) && !hasPlanted)
            species = ((Tree) block.getState().getData()).getSpecies();
        block.breakNaturally(event.getPlayer().getItemInHand());
        if(species != null) {
            block.setTypeId(BlockID.SAPLING);
            ((Tree) block.getState().getData()).setSpecies(species);
            hasPlanted = true;
        }
        visitedLocations.add(block.getLocation());
        broken += 1;
        for(BlockFace face : plugin.getConfiguration().treeLopperAllowDiagonals ? LocationUtil.getIndirectFaces() : LocationUtil.getDirectFaces()) {
            if(block.getRelative(face).getTypeId() == usedBlock.getTypeId() && (!plugin.getConfiguration().treeLopperEnforceData || block.getRelative(face).getData() == usedBlock.getData()))
                if(searchBlock(event, block.getRelative(face))) {
                    ItemStack heldItem = event.getPlayer().getItemInHand();
                    if(heldItem != null && ItemUtil.getMaxDurability(heldItem.getTypeId()) > 0) {
                        heldItem.setDurability((short) (heldItem.getDurability() + 1));
                        if(heldItem.getDurability() <= ItemUtil.getMaxDurability(heldItem.getTypeId()))
                            event.getPlayer().setItemInHand(heldItem);
                        else
                            event.getPlayer().setItemInHand(null);
                    }
                }
        }

        return true;
    }

    public static class Factory extends AbstractMechanicFactory<TreeLopper> {

        @Override
        public TreeLopper detect(BlockWorldVector pt, LocalPlayer player) {

            Block block = BukkitUtil.toWorld(pt).getBlockAt(BukkitUtil.toLocation(pt));
            if (CraftBookPlugin.inst().getConfiguration().treeLopperBlocks.contains(new ItemInfo(block))) {
                return CraftBookPlugin.inst().getConfiguration().treeLopperItems.contains(player.getHeldItemInfo()) && player.hasPermission("craftbook.mech.treelopper.use") ? new TreeLopper() : null;
            }
            return null;
        }
    }
}