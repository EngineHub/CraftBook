package com.sk89q.craftbook.mechanics;

import com.sk89q.craftbook.AbstractCraftBookMechanic;
import com.sk89q.craftbook.CraftBookPlayer;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.util.EventUtil;
import com.sk89q.craftbook.util.InventoryUtil;
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
import java.util.Objects;

public class BetterLeads extends AbstractCraftBookMechanic {

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerClick(final PlayerInteractEntityEvent event) {
        if(!ItemUtil.isStackValid(InventoryUtil.getItemInHand(event.getPlayer(), event.getHand()))) return;
        if(!(event.getRightClicked() instanceof LivingEntity)) return;
        CraftBookPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());
        if(InventoryUtil.getItemInHand(event.getPlayer(), event.getHand()).getType() != Material.LEAD) return;

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
        if(event.getRightClicked() instanceof Creature && ((Creature) event.getRightClicked()).getTarget() != null && ((Creature) event.getRightClicked()).getTarget().equals(event.getPlayer()))
            ((Creature) event.getRightClicked()).setTarget(null); //Rescan for a new target.
        event.setCancelled(true);
        Bukkit.getScheduler().runTask(CraftBookPlugin.inst(), () -> {
            if(!((LivingEntity) event.getRightClicked()).setLeashHolder(event.getPlayer())) {
                CraftBookPlugin.logDebugMessage("Failed to leash entity!", "betterleads.allowed-mobs");
            }
        });
        if(event.getPlayer().getGameMode() == GameMode.CREATIVE)
            return;
        if(InventoryUtil.getItemInHand(event.getPlayer(), event.getHand()).getAmount() == 1)
            InventoryUtil.setItemInHand(event.getPlayer(), event.getHand(), null);
        else {
            ItemStack newStack = InventoryUtil.getItemInHand(event.getPlayer(), event.getHand());
            newStack.setAmount(newStack.getAmount() - 1);
            InventoryUtil.setItemInHand(event.getPlayer(), event.getHand(), newStack);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityTarget(EntityTargetEvent event) {

        if(!leadsStopTarget && !leadsMobRepellant) return;
        if(!(event.getEntity() instanceof Monster)) return;
        if(!((LivingEntity) event.getEntity()).isLeashed()) return;
        if(!(event.getTarget() instanceof Player)) return;

        if (!EventUtil.passesFilter(event)) return;

        CraftBookPlayer player = CraftBookPlugin.inst().wrapPlayer((Player) event.getTarget());

        if(leadsStopTarget && player.hasPermission("craftbook.mech.leads.ignore-target")) {
            if(((LivingEntity) event.getEntity()).getLeashHolder().equals(event.getTarget())) {
                event.setTarget(null);
                event.setCancelled(true);
                return;
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
                    return;
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
            //Still needs to be used by further plugins in the event. We wouldn't want bukkit complaining now, would we?
            Bukkit.getScheduler().runTask(CraftBookPlugin.inst(), event.getEntity()::remove);
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
                event.getEntity().getWorld().dropItemNaturally(event.getEntity().getLocation(), new ItemStack(Material.LEAD, 1));
            } else {
                amountConnected++;
            }
        }

        if(!leadsHitchPersists && amountConnected == 0) {
            //Still needs to be used by further plugins in the event. We wouldn't want bukkit complaining now, would we?
            Bukkit.getScheduler().runTask(CraftBookPlugin.inst(), event.getEntity()::remove);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onUnleash(PlayerUnleashEntityEvent event) {

        if(!leadsOwnerBreakOnly) return;
        if(!(event.getEntity() instanceof LivingEntity)) return;
        if(!((LivingEntity) event.getEntity()).isLeashed() || !(((LivingEntity) event.getEntity()).getLeashHolder() instanceof LeashHitch)) return;
        if(!(event.getEntity() instanceof Tameable)) return;
        if(!((Tameable) event.getEntity()).isTamed()) return;

        if (!EventUtil.passesFilter(event)) return;

        if(!Objects.equals(((Tameable) event.getEntity()).getOwner(), event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    private boolean leadsStopTarget;
    private boolean leadsOwnerBreakOnly;
    private boolean leadsHitchPersists;
    private boolean leadsMobRepellant;
    private List<String> leadsAllowedMobs;

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