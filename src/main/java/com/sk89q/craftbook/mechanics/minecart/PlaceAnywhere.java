package com.sk89q.craftbook.mechanics.minecart;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Minecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.util.EventUtil;
import com.sk89q.craftbook.util.RailUtil;
import com.sk89q.util.yaml.YAMLProcessor;

public class PlaceAnywhere extends AbstractCraftBookMechanic {

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerClick(PlayerInteractEvent event) {

        if(!EventUtil.passesFilter(event)) return;

        if(event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getHand() != EquipmentSlot.HAND) return;
        if(event.getPlayer().getItemInHand() == null || event.getPlayer().getItemInHand().getType() != Material.MINECART) return;
        if(RailUtil.isTrack(event.getClickedBlock().getType())) return;

        Location loc = event.getClickedBlock().getRelative(0, 2, 0).getLocation();
        event.getClickedBlock().getWorld().spawn(loc, Minecart.class);
        if(event.getPlayer().getGameMode() != GameMode.CREATIVE) {
            if(event.getPlayer().getItemInHand().getAmount() <= 1)
                event.getPlayer().setItemInHand(null);
            else {
                ItemStack heldItem = event.getPlayer().getItemInHand();
                heldItem.setAmount(heldItem.getAmount() - 1);
                event.getPlayer().setItemInHand(heldItem);
            }
            event.setCancelled(true);
        }
    }

    @Override
    public void loadConfiguration (YAMLProcessor config, String path) {

    }
}