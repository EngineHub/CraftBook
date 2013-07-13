package com.sk89q.craftbook.vehicles.boat;

import org.bukkit.Bukkit;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.util.EntityUtil;
import com.sk89q.worldedit.blocks.ItemID;

public class BoatExitRemover implements Listener {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onVehicleExit(final VehicleExitEvent event) {

        if (!(event.getVehicle() instanceof Boat)) return;

        Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), new Runnable() {

            @Override
            public void run () {
                if(CraftBookPlugin.inst().getConfiguration().boatRemoveOnExitGiveItem) {

                    ItemStack stack = new ItemStack(ItemID.WOOD_BOAT, 1);

                    if(event.getExited() instanceof Player) {
                        if(!((Player) event.getExited()).getInventory().addItem(stack).isEmpty())
                            event.getExited().getWorld().dropItemNaturally(event.getExited().getLocation(), stack);
                    } else if(event.getExited() != null)
                        event.getExited().getWorld().dropItemNaturally(event.getExited().getLocation(), stack);
                }
                EntityUtil.killEntity(event.getVehicle());
            }
        }, 2L);
    }
}