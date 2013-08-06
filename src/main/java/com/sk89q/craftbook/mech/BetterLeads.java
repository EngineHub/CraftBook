package com.sk89q.craftbook.mech;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.util.ItemUtil;
import com.sk89q.worldedit.blocks.ItemID;

public class BetterLeads implements Listener {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerClick(PlayerInteractEntityEvent event) {

        if(!ItemUtil.isStackValid(event.getPlayer().getItemInHand())) return;
        if(!(event.getRightClicked() instanceof LivingEntity)) return;
        LocalPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());
        if(player.getHeldItemType() != ItemID.LEAD) return;

        String typeName = event.getRightClicked().getType().getName();
        if (typeName == null && event.getRightClicked().getType() == EntityType.PLAYER)
            typeName = "PLAYER";
        else if (typeName == null)
            return; //Invalid type.

        boolean found = false;
        for(String type : CraftBookPlugin.inst().getConfiguration().leadsAllowedMobs) {
            if(type.equalsIgnoreCase(typeName)) {
                found = true;
                break;
            }
        }

        if(!found)
            return;

        if(!player.hasPermission("craftbook.mech.leads")) {
            if(CraftBookPlugin.inst().getConfiguration().showPermissionMessages)
                player.printError("mech.use-permission");
            return;
        }

        ((LivingEntity) event.getRightClicked()).setLeashHolder(event.getPlayer());
    }
}