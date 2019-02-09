package com.sk89q.craftbook.mechanics.boat;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.util.EntityUtil;
import com.sk89q.craftbook.util.EventUtil;
import com.sk89q.craftbook.util.ItemUtil;
import com.sk89q.util.yaml.YAMLProcessor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Boat;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.inventory.ItemStack;

public class ExitRemover extends AbstractCraftBookMechanic {

    @EventHandler(priority = EventPriority.HIGH)
    public void onVehicleExit(VehicleExitEvent event) {

        if(!EventUtil.passesFilter(event)) return;

        if (!(event.getVehicle() instanceof Boat)) return;

        Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), new BoatRemover(event.getExited(), (Boat) event.getVehicle()), 2L);
    }

    class BoatRemover implements Runnable {

        LivingEntity player;
        Boat boat;

        BoatRemover(LivingEntity player, Boat boat) {
            this.player = player;
            this.boat = boat;
        }

        @Override
        public void run () {

            if(!boat.isValid() || boat.isDead() || !boat.isEmpty()) return;

            if(giveItem) {
                ItemStack stack = new ItemStack(ItemUtil.getBoatFromTree(boat.getWoodType()), 1);

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

    boolean giveItem;

    @Override
    public void loadConfiguration (YAMLProcessor config, String path) {

        config.setComment(path + "give-item", "Sets whether to give the player the item back or not.");
        giveItem = config.getBoolean(path + "give-item", false);
    }
}