package com.sk89q.craftbook.vehicles.boat;

import org.bukkit.block.Block;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.worldedit.blocks.BlockID;

public class BoatWaterPlaceOnly extends AbstractCraftBookMechanic {

    @EventHandler
    public void playerInteractEvent(PlayerInteractEvent event) {

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block above = event.getClickedBlock().getRelative(0,1,0);
            if ((!isWater(above) || event.getClickedBlock().getY() == event.getClickedBlock().getWorld().getMaxHeight() - 1) && !isWater(event.getClickedBlock())) {
                event.setCancelled(true);
                event.setUseItemInHand(Result.DENY);
            }
        }
    }

    private boolean isWater(Block b) {

        return b.getTypeId() == BlockID.WATER || b.getTypeId() == BlockID.STATIONARY_WATER;
    }
}