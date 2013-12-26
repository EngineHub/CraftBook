package com.sk89q.craftbook.vehicles.cart;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.RideableMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.util.CartUtil;
import com.sk89q.craftbook.util.EntityUtil;

public class CartExitRemover extends AbstractCraftBookMechanic {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onVehicleExit(final VehicleExitEvent event) {

        if (!(event.getVehicle() instanceof RideableMinecart)) return;
        if (event.getVehicle().isDead() || !event.getVehicle().isValid()) return;

        Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), new Runnable() {

            @Override
            public void run () {
                if (event.getVehicle().isDead() || !event.getVehicle().isValid()) return;
                if(CraftBookPlugin.inst().getConfiguration().minecartRemoveOnExitGiveItem) {

                    ItemStack stack = CartUtil.getCartStack((Minecart) event.getVehicle());

                    if(event.getExited() instanceof Player) {
                        if(!((Player) event.getExited()).getInventory().addItem(stack).isEmpty() && ((Player) event.getExited()).getGameMode() != GameMode.CREATIVE)
                            event.getExited().getWorld().dropItemNaturally(event.getExited().getLocation(), stack);
                    } else if(event.getExited() != null)
                        event.getExited().getWorld().dropItemNaturally(event.getExited().getLocation(), stack);
                }
                EntityUtil.killEntity(event.getVehicle());
            }
        }, 2L);
    }
}