package com.sk89q.craftbook.mechanics;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.util.EventUtil;
import com.sk89q.craftbook.util.ItemUtil;
import com.sk89q.util.yaml.YAMLProcessor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerUnleashEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

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
        for(String type : leadsAllowedMobs) {
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

        if(!leadsStopTarget && !leadsMobRepellant) return;
        if(!(event.getEntity() instanceof Monster)) return;
        if(!((LivingEntity) event.getEntity()).isLeashed()) return;
        if(!(event.getTarget() instanceof Player)) return;

        if (!EventUtil.passesFilter(event)) return;

        LocalPlayer player = CraftBookPlugin.inst().wrapPlayer((Player) event.getTarget());

        if(leadsStopTarget && player.hasPermission("craftbook.mech.leads.ignore-target")) {
            if(((LivingEntity) event.getEntity()).getLeashHolder().equals(event.getTarget())) {
                event.setTarget(null);
                event.setCancelled(true);
            }
        }

        if(leadsMobRepellant && player.hasPermission("craftbook.mech.leads.mob-repel")) {
            for(Entity ent : event.getTarget().getNearbyEntities(5, 5, 5)) {
                if(ent == null || !ent.isValid()) continue;
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

        if(!leadsHitchPersists) return;
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

        if(!leadsHitchPersists && !leadsOwnerBreakOnly) return;
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
                if(!((Tameable) ent).isTamed() || ((Tameable) ent).getOwner().equals(event.getRemover()))
                    isOwner = true;
            if(isOwner || !(ent instanceof Tameable) || !leadsOwnerBreakOnly || event.getRemover().hasPermission("craftbook.mech.leads.owner-break-only.bypass")) {
                ((LivingEntity) ent).setLeashHolder(null);
                event.getEntity().getWorld().dropItemNaturally(event.getEntity().getLocation(), new ItemStack(Material.LEASH, 1));
            } else {
                amountConnected++;
            }
        }

        if(!leadsHitchPersists && amountConnected == 0) {
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

        if(!leadsOwnerBreakOnly) return;
        if(!(event.getEntity() instanceof LivingEntity)) return;
        if(!((LivingEntity) event.getEntity()).isLeashed() || !(((LivingEntity) event.getEntity()).getLeashHolder() instanceof LeashHitch)) return;
        if(!(event.getEntity() instanceof Tameable)) return;
        if(!!((Tameable) event.getEntity()).isTamed()) return; // TODO is this right?

        if (!EventUtil.passesFilter(event)) return;

        if(!((Tameable) event.getEntity()).getOwner().equals(event.getPlayer()))
            event.setCancelled(true);
    }

    boolean leadsStopTarget;
    boolean leadsOwnerBreakOnly;
    boolean leadsHitchPersists;
    boolean leadsMobRepellant;
    List<String> leadsAllowedMobs;

    @Override
    public void loadConfiguration (YAMLProcessor config, String path) {

        config.setComment(path + "stop-mob-target", "Stop hostile mobs targeting you if you are holding them on a leash.");
        leadsStopTarget = config.getBoolean(path + "stop-mob-target", false);

        config.setComment(path + "owner-unleash-only", "Only allow the owner of tameable entities to unleash them from a leash hitch.");
        leadsOwnerBreakOnly = config.getBoolean(path + "owner-unleash-only", false);

        config.setComment(path + "hitch-persists", "Stop leash hitches breaking when clicked no entities are attached. This allows for a public horse hitch or similar.");
        leadsHitchPersists = config.getBoolean(path + "hitch-persists", false);

        config.setComment(path + "mob-repel", "If you have a mob tethered to you, mobs of that type will not target you.");
        leadsMobRepellant = config.getBoolean(path + "mob-repel", false);

        config.setComment(path + "allowed-mobs", "The list of mobs that can be tethered with a lead.");
        leadsAllowedMobs = config.getStringList(path + "allowed-mobs", Arrays.asList("ZOMBIE","SPIDER"));
    }
}