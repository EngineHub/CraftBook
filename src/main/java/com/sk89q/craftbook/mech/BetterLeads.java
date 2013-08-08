package com.sk89q.craftbook.mech;

import org.bukkit.entity.Creature;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;

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
        CraftBookPlugin.logDebugMessage("A player has right clicked an entity with a lead!", "betterleads.allowed-mobs");

        String typeName = event.getRightClicked().getType().getName();
        if (typeName == null && event.getRightClicked().getType() == EntityType.PLAYER)
            typeName = "PLAYER";
        else if (typeName == null)
            return; //Invalid type.

        CraftBookPlugin.logDebugMessage("It is of type: " + typeName, "betterleads.allowed-mobs");

        boolean found = false;
        for(String type : CraftBookPlugin.inst().getConfiguration().leadsAllowedMobs) {
            if(type.equalsIgnoreCase(typeName)) {
                found = true;
                break;
            }
        }

        if(!found)
            return;

        CraftBookPlugin.logDebugMessage(typeName + " is allowed in the configuration.", "betterleads.allowed-mobs");

        if(!player.hasPermission("craftbook.mech.leads") || !player.hasPermission("craftbook.mech.leads.mobs." + typeName.toLowerCase())) {
            if(CraftBookPlugin.inst().getConfiguration().showPermissionMessages)
                player.printError("mech.use-permission");
            return;
        }

        CraftBookPlugin.logDebugMessage("Leashing entity!", "betterleads.allowed-mobs");
        if(!((LivingEntity) event.getRightClicked()).setLeashHolder(event.getPlayer()))
            CraftBookPlugin.logDebugMessage("Failed to leash entity!", "betterleads.allowed-mobs");
        else {
            if(((Creature) event.getRightClicked()).getTarget().equals(event.getPlayer()))
                ((Creature) event.getRightClicked()).setTarget(null); //Rescan for a new target.
            event.setCancelled(true);
            if(event.getPlayer().getItemInHand().getAmount() == 1)
                event.getPlayer().setItemInHand(null);
            else {
                ItemStack newStack = event.getPlayer().getItemInHand();
                newStack.setAmount(newStack.getAmount() - 1);
                event.getPlayer().setItemInHand(newStack);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onEntityTarget(EntityTargetEvent event) {

        if(!(event.getEntity() instanceof Monster)) return;
        if(!((LivingEntity) event.getEntity()).isLeashed()) return;
        if(!(event.getTarget() instanceof Player)) return;
        if(!CraftBookPlugin.inst().getConfiguration().leadsStopTarget) return;

        LocalPlayer player = CraftBookPlugin.inst().wrapPlayer((Player) event.getTarget());

        if(!player.hasPermission("craftbook.mech.leads") || !player.hasPermission("craftbook.mech.leads.ignore-target"))
            return;

        if(((LivingEntity) event.getEntity()).getLeashHolder().equals(event.getTarget()))
            event.setCancelled(true);
    }
}