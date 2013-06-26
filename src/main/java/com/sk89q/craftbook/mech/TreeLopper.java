package com.sk89q.craftbook.mech;

import java.util.HashSet;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.block.BlockBreakEvent;

import com.sk89q.craftbook.AbstractMechanic;
import com.sk89q.craftbook.AbstractMechanicFactory;
import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.MechanicListenerAdapter;
import com.sk89q.craftbook.util.LocationUtil;
import com.sk89q.worldedit.BlockWorldVector;
import com.sk89q.worldedit.bukkit.BukkitUtil;

public class TreeLopper extends AbstractMechanic {

    protected final CraftBookPlugin plugin = CraftBookPlugin.inst();

    private int broken;
    private int blockId;
    private byte blockData;

    private HashSet<Location> visitedLocations = new HashSet<Location>();

    @Override
    public void onBlockBreak(BlockBreakEvent event) {

        if(MechanicListenerAdapter.ignoredEvents.contains(event))
            return;
        if(event.getPlayer().getGameMode() == GameMode.CREATIVE)
            return;

        blockId = event.getBlock().getTypeId();
        blockData = event.getBlock().getData();
        visitedLocations.add(event.getBlock().getLocation());
        broken = 1;

        Block block = event.getBlock();

        for(BlockFace face : plugin.getConfiguration().treeLopperAllowDiagonals ? LocationUtil.getIndirectFaces() : LocationUtil.getDirectFaces()) {
            if(block.getRelative(face).getTypeId() == blockId && (!plugin.getConfiguration().treeLopperEnforceData || block.getRelative(face).getData() == blockData))
                searchBlock(event, block.getRelative(face));
        }
    }

    public void searchBlock(BlockBreakEvent event, Block block) {

        if(visitedLocations.contains(block.getLocation()))
            return;
        if(broken > plugin.getConfiguration().treeLopperMaxSize)
            return;
        if(!plugin.canBuild(event.getPlayer(), block, false)) {
            event.getPlayer().sendMessage(ChatColor.RED + "You do not have permission to build in this area!");
            return;
        }
        block.breakNaturally(event.getPlayer().getItemInHand());
        visitedLocations.add(block.getLocation());
        broken += 1;
        for(BlockFace face : plugin.getConfiguration().treeLopperAllowDiagonals ? LocationUtil.getIndirectFaces() : LocationUtil.getDirectFaces()) {
            if(block.getRelative(face).getTypeId() == blockId && (!plugin.getConfiguration().treeLopperEnforceData || block.getRelative(face).getData() == blockData))
                searchBlock(event, block.getRelative(face));
        }
    }

    public static class Factory extends AbstractMechanicFactory<TreeLopper> {

        @Override
        public TreeLopper detect(BlockWorldVector pt, LocalPlayer player) {

            Block block = BukkitUtil.toWorld(pt).getBlockAt(BukkitUtil.toLocation(pt));
            if (CraftBookPlugin.inst().getConfiguration().treeLopperBlocks.contains(block.getTypeId())) {
                return CraftBookPlugin.inst().getConfiguration().treeLopperItems.contains(player.getHeldItemType()) && player.hasPermission("craftbook.mech.treelopper.use") ? new TreeLopper() : null;
            }
            return null;
        }
    }
}