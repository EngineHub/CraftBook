package com.sk89q.craftbook.mechanics;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LeashHitch;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerUnleashEntityEvent;
import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.util.EventUtil;
import com.sk89q.craftbook.util.ItemUtil;

public class BetterLeads extends AbstractCraftBookMechanic {

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerClick(PlayerInteractEntityEvent event) {

        if(!ItemUtil.isStackValid(event.getPlayer().getItemInHand())) return;
        if(!(event.getRightClicked() instanceof LivingEntity)) return;
        LocalPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());
        if(player.getHeldItemInfo().getType() != Material.LEASH) return;

        if (!EventUtil.passesFilter(event)) return;

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

        if(!player.hasPermission("craftbook.mech.leads") && !player.hasPermission("craftbook.mech.leads.mobs." + typeName.toLowerCase())) {
            if(CraftBookPlugin.inst().getConfiguration().showPermissionMessages)
                player.printError("mech.use-permission");
            return;
        }

        CraftBookPlugin.logDebugMessage("Leashing entity!", "betterleads.allowed-mobs");
        if(!((LivingEntity) event.getRightClicked()).setLeashHolder(event.getPlayer()))
            CraftBookPlugin.logDebugMessage("Failed to leash entity!", "betterleads.allowed-mobs");
        else {
            if(event.getRightClicked() instanceof Creature && ((Creature) event.getRightClicked()).getTarget() != null && ((Creature) event.getRightClicked()).getTarget().equals(event.getPlayer()))
                ((Creature) event.getRightClicked()).setTarget(null); //Rescan for a new target.
            event.setCancelled(true);
            if(event.getPlayer().getGameMode() == GameMode.CREATIVE)
                return;
            if(event.getPlayer().getItemInHand().getAmount() == 1)
                event.getPlayer().setItemInHand(null);
            else {
                ItemStack newStack = event.getPlayer().getItemInHand();
                newStack.setAmount(newStack.getAmount() - 1);
                event.getPlayer().setItemInHand(newStack);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityTarget(EntityTargetEvent event) {

        if(!CraftBookPlugin.inst().getConfiguration().leadsStopTarget && !CraftBookPlugin.inst().getConfiguration().leadsMobRepellant) return;
        if(!(event.getEntity() instanceof Monster)) return;
        if(!((LivingEntity) event.getEntity()).isLeashed()) return;
        if(!(event.getTarget() instanceof Player)) return;

        if (!EventUtil.passesFilter(event)) return;

        LocalPlayer player = CraftBookPlugin.inst().wrapPlayer((Player) event.getTarget());

        if(CraftBookPlugin.inst().getConfiguration().leadsStopTarget && player.hasPermission("craftbook.mech.leads.ignore-target")) {
            if(((LivingEntity) event.getEntity()).getLeashHolder().equals(event.getTarget())) {
                event.setTarget(null);
                event.setCancelled(true);
            }
        }

        if(CraftBookPlugin.inst().getConfiguration().leadsMobRepellant && player.hasPermission("craftbook.mech.leads.mob-repel")) {
            for(Entity ent : event.getTarget().getNearbyEntities(5, 5, 5)) {
                if(ent.getType() != event.getEntity().getType())
                    continue;
                if(((LivingEntity) ent).getLeashHolder().equals(event.getTarget())) {
                    event.setTarget(null);
                    event.setCancelled(true);
                    break;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onHitchBreakRandomly(final HangingBreakEvent event) {

        if(!CraftBookPlugin.inst().getConfiguration().leadsHitchPersists) return;
        if(!(event.getEntity() instanceof LeashHitch)) return;

        if (!EventUtil.passesFilter(event)) return;

        int amountConnected = 0;

        for(Entity ent : event.getEntity().getNearbyEntities(10, 10, 10)) {
            if(!(ent instanceof LivingEntity)) continue;
            if(!((LivingEntity) ent).isLeashed() || !((LivingEntity) ent).getLeashHolder().equals(event.getEntity())) continue;
            amountConnected++;
        }

        if(amountConnected == 0) {
            Bukkit.getScheduler().runTask(CraftBookPlugin.inst(), new Runnable() {
                @Override
                public void run () {
                    event.getEntity().remove(); //Still needs to be used by further plugins in the event. We wouldn't want bukkit complaining now, would we?
                }
            });
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onHitchBreak(final HangingBreakByEntityEvent event) {

        if(!CraftBookPlugin.inst().getConfiguration().leadsHitchPersists && !CraftBookPlugin.inst().getConfiguration().leadsOwnerBreakOnly) return;
        if(!(event.getEntity() instanceof LeashHitch)) return;
        if(!(event.getRemover() instanceof Player)) return;

        if (!EventUtil.passesFilter(event)) return;

        event.setCancelled(true);

        int amountConnected = 0;

        for(Entity ent : event.getEntity().getNearbyEntities(10, 10, 10)) {
            if(!(ent instanceof LivingEntity)) continue;
            if(!((LivingEntity) ent).isLeashed() || !((LivingEntity) ent).getLeashHolder().equals(event.getEntity())) continue;
            boolean isOwner = false;
            if(ent instanceof Tameable)
                if(!((Tameable) event.getEntity()).isTamed() || ((Tameable) ent).getOwner().equals(event.getRemover()))
                    isOwner = true;
            if(isOwner || !(ent instanceof Tameable) || !CraftBookPlugin.inst().getConfiguration().leadsOwnerBreakOnly || ((Player) event.getRemover()).hasPermission("craftbook.mech.leads.owner-break-only.bypass")) {
                ((LivingEntity) ent).setLeashHolder(null);
                event.getEntity().getWorld().dropItemNaturally(event.getEntity().getLocation(), new ItemStack(Material.LEASH, 1));
                continue;
            } else
                amountConnected++;
        }

        if(!CraftBookPlugin.inst().getConfiguration().leadsHitchPersists && amountConnected == 0) {
            Bukkit.getScheduler().runTask(CraftBookPlugin.inst(), new Runnable() {
                @Override
                public void run () {
                    event.getEntity().remove(); //Still needs to be used by further plugins in the event. We wouldn't want bukkit complaining now, would we?
                }
            });
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onUnleash(PlayerUnleashEntityEvent event) {

        if(!CraftBookPlugin.inst().getConfiguration().leadsOwnerBreakOnly) return;
        if(!(event.getEntity() instanceof LivingEntity)) return;
        if(!((LivingEntity) event.getEntity()).isLeashed() || !(((LivingEntity) event.getEntity()).getLeashHolder() instanceof LeashHitch)) return;
        if(!(event.getEntity() instanceof Tameable)) return;
        if(!!((Tameable) event.getEntity()).isTamed()) return;

        if (!EventUtil.passesFilter(event)) return;

        if(!((Tameable) event.getEntity()).getOwner().equals(event.getPlayer()))
            event.setCancelled(true);
    }
}