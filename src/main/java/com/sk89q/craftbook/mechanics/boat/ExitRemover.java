package com.sk89q.craftbook.mechanics.boat;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Boat;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.util.EntityUtil;
import com.sk89q.craftbook.util.EventUtil;

public class ExitRemover extends AbstractCraftBookMechanic {

    @EventHandler(priority = EventPriority.HIGH)
    public void onVehicleExit(VehicleExitEvent event) {

        if(!EventUtil.passesFilter(event)) return;

        if (!(event.getVehicle() instanceof Boat)) return;

        Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), new BoatRemover(event.getExited(), (Boat) event.getVehicle()), 2L);
    }

    public class BoatRemover implements Runnable {

        LivingEntity player;
        Boat boat;

        public BoatRemover(LivingEntity player, Boat boat) {
            this.player = player;
            this.boat = boat;
        }

        @Override
        public void run () {

            if(!boat.isValid() || boat.isDead()) return;

            if(CraftBookPlugin.inst().getConfiguration().boatRemoveOnExitGiveItem) {
                ItemStack stack = new ItemStack(Material.BOAT, 1);

                if(player instanceof Player) {
                    if(!((Player) player).getInventory().addItem(stack).isEmpty())
                        player.getLocation().getWorld().dropItemNaturally(player.getLocation(), stack);
                } else if(player != null)
                    player.getLocation().getWorld().dropItemNaturally(player.getLocation(), stack);
                else
                    boat.getLocation().getWorld().dropItemNaturally(boat.getLocation(), stack);
            }
            EntityUtil.killEntity(boat);
        }
    }
}