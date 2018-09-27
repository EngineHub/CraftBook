package com.sk89q.craftbook.mechanics.minecart;

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
import com.sk89q.craftbook.util.EventUtil;
import com.sk89q.util.yaml.YAMLProcessor;

public class ExitRemover extends AbstractCraftBookMechanic {

    @EventHandler(priority = EventPriority.HIGH)
    public void onVehicleExit(final VehicleExitEvent event) {

        if (!(event.getVehicle() instanceof RideableMinecart)) return;
        if (event.getVehicle().isDead() || !event.getVehicle().isValid()) return;

        if(!EventUtil.passesFilter(event)) return;

        if(CraftBookPlugin.inst().isMechanicEnabled(TemporaryCart.class)) {
            if(((TemporaryCart) CraftBookPlugin.inst().getMechanic(TemporaryCart.class)).getMinecarts().contains(event.getVehicle()))
                return;
        }

        Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), () -> {

            if (event.getVehicle().isDead() || !event.getVehicle().isValid()) return;

            if(giveItem) {

                ItemStack stack = CartUtil.getCartStack((Minecart) event.getVehicle());

                if(event.getExited() instanceof Player) {
                    if(!((Player) event.getExited()).getInventory().addItem(stack).isEmpty() && ((Player) event.getExited()).getGameMode() != GameMode.CREATIVE)
                        event.getExited().getWorld().dropItemNaturally(event.getExited().getLocation(), stack);
                } else if(event.getExited() != null)
                    event.getExited().getWorld().dropItemNaturally(event.getExited().getLocation(), stack);
            }
            EntityUtil.killEntity(event.getVehicle());
        }, 2L);
    }

    private boolean giveItem;

    @Override
    public void loadConfiguration (YAMLProcessor config, String path) {

        config.setComment(path + "give-item", "Sets whether to give the player the item back or not.");
        giveItem = config.getBoolean(path + "give-item", false);
    }
}