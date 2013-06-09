package com.sk89q.craftbook.mech;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.block.BlockBreakEvent;

import com.sk89q.craftbook.AbstractMechanic;
import com.sk89q.craftbook.AbstractMechanicFactory;
import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.util.LocationUtil;
import com.sk89q.worldedit.BlockWorldVector;
import com.sk89q.worldedit.bukkit.BukkitUtil;

public class TreeLopper extends AbstractMechanic {

    protected final CraftBookPlugin plugin = CraftBookPlugin.inst();

    private int broken;
    private int blockId;
    private byte blockData;

    @Override
    public void onBlockBreak(BlockBreakEvent event) {

        blockId = event.getBlock().getTypeId();
        blockData = event.getBlock().getData();
        event.getBlock().breakNaturally(event.getPlayer().getItemInHand());
        broken = 1;

        Block block = event.getBlock();

        for(BlockFace face : plugin.getConfiguration().treeLopperAllowDiagonals ? LocationUtil.getIndirectFaces() : LocationUtil.getDirectFaces()) {
            if(block.getRelative(face).getTypeId() == blockId && block.getRelative(face).getData() == blockData)
                searchBlock(event, block.getRelative(face));
        }
    }

    public void searchBlock(BlockBreakEvent event, Block block) {

        if(broken > plugin.getConfiguration().treeLopperMaxSize)
            return;
        if(!plugin.canBuild(event.getPlayer(), block, false)) {
            event.getPlayer().sendMessage(ChatColor.RED + "You do not have permission to build in this area!");
            return;
        }
        block.breakNaturally(event.getPlayer().getItemInHand());
        broken += 1;
        for(BlockFace face : plugin.getConfiguration().treeLopperAllowDiagonals ? LocationUtil.getIndirectFaces() : LocationUtil.getDirectFaces()) {
            if(block.getRelative(face).getTypeId() == blockId && block.getRelative(face).getData() == blockData)
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