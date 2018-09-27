package com.sk89q.craftbook.mechanics.boat;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.util.yaml.YAMLProcessor;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class WaterPlaceOnly extends AbstractCraftBookMechanic {

    @EventHandler
    public void playerInteractEvent(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if(event.getItem() != null
                    && Tag.ITEMS_BOATS.isTagged(event.getItem().getType())) {
                Block above = event.getClickedBlock().getRelative(0,1,0);
                if ((!isWater(above) || event.getClickedBlock().getY() == event.getClickedBlock().getWorld().getMaxHeight() - 1) && !isWater(event.getClickedBlock())) {
                    event.setCancelled(true);
                    event.setUseItemInHand(Result.DENY);
                    event.getPlayer().sendMessage(ChatColor.RED + "You can't place that boat on land!");
                }
            }
        }
    }

    private static boolean isWater(Block b) {
        return b.getType() == Material.WATER;
    }

    @Override
    public void loadConfiguration (YAMLProcessor config, String path) {

    }
}