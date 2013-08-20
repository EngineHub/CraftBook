package com.sk89q.craftbook.vehicles.boat;

import org.bukkit.Bukkit;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.util.EntityUtil;
import com.sk89q.worldedit.blocks.ItemID;

public class BoatExitRemover extends AbstractCraftBookMechanic {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onVehicleExit(VehicleExitEvent event) {

        if (!(event.getVehicle() instanceof Boat)) return;

        Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), new BoatRemover(event), 2L);
    }

    public class BoatRemover implements Runnable {

        VehicleExitEvent event;

        public BoatRemover(VehicleExitEvent event) {
            this.event = event;
        }

        @Override
        public void run () {
            if(CraftBookPlugin.inst().getConfiguration().boatRemoveOnExitGiveItem) {

                ItemStack stack = new ItemStack(ItemID.WOOD_BOAT, 1);

                if(event.getExited() instanceof Player) {
                    if(!((Player) event.getExited()).getInventory().addItem(stack).isEmpty())
                        event.getExited().getLocation().getWorld().dropItemNaturally(event.getExited().getLocation(), stack);
                } else if(event.getExited() != null)
                    event.getExited().getLocation().getWorld().dropItemNaturally(event.getExited().getLocation(), stack);
                else
                    event.getVehicle().getLocation().getWorld().dropItemNaturally(event.getVehicle().getLocation(), stack);
            }
            EntityUtil.killEntity(event.getVehicle());
        }
    }
}