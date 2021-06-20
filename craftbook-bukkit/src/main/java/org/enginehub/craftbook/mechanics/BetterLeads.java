/*
 * CraftBook Copyright (C) EngineHub and Contributors <https://enginehub.org/>
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not,
 * see <http://www.gnu.org/licenses/>.
 */

package org.enginehub.craftbook.mechanics;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.world.entity.EntityTypes;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LeashHitch;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerUnleashEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.enginehub.craftbook.AbstractCraftBookMechanic;
import org.enginehub.craftbook.CraftBook;
import org.enginehub.craftbook.CraftBookPlayer;
import org.enginehub.craftbook.bukkit.CraftBookPlugin;
import org.enginehub.craftbook.util.EventUtil;

import java.util.Set;

public class BetterLeads extends AbstractCraftBookMechanic {

    private final static int MAX_LEASH_DISTANCE = 10;

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerClick(final PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof LivingEntity) || !EventUtil.passesFilter(event)) {
            return;
        }

        CraftBookPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());
        if (event.getPlayer().getInventory().getItem(event.getHand()).getType() != Material.LEAD) {
            return;
        }

        CraftBookPlugin.logDebugMessage("A player has right clicked an entity with a lead!", "betterleads.allowed-mobs");

        String typeName = BukkitAdapter.adapt(event.getRightClicked().getType()).getId();
        if (typeName == null) {
            return; //Invalid type.
        }

        CraftBookPlugin.logDebugMessage("It is of type: " + typeName, "betterleads.allowed-mobs");

        if (!allowedMobs.contains(typeName)
            || !typeName.startsWith("minecraft:")
            || !allowedMobs.contains(typeName.substring("minecraft:".length()))) {
            return;
        }

        CraftBookPlugin.logDebugMessage(typeName + " is allowed in the configuration.", "betterleads.allowed-mobs");

        if (!player.hasPermission("craftbook.betterleads.leash") && !player.hasPermission("craftbook.betterleads.leash." + typeName.replace(":", "."))) {
            if (CraftBook.getInstance().getPlatform().getConfiguration().showPermissionMessages) {
                player.printError("mech.use-permission");
            }
            return;
        }

        CraftBookPlugin.logDebugMessage("Leashing entity!", "betterleads.allowed-mobs");
        if (event.getRightClicked() instanceof Creature && ((Creature) event.getRightClicked()).getTarget() != null && ((Creature) event.getRightClicked()).getTarget().equals(event.getPlayer())) {
            ((Creature) event.getRightClicked()).setTarget(null); //Rescan for a new target.
        }

        event.setCancelled(true);

        Bukkit.getScheduler().runTask(CraftBookPlugin.inst(), () -> {
            if (!((LivingEntity) event.getRightClicked()).setLeashHolder(event.getPlayer())) {
                CraftBookPlugin.logDebugMessage("Failed to leash entity!", "betterleads.allowed-mobs");
                return;
            }

            // Don't take items in creative mode.
            if (event.getPlayer().getGameMode() == GameMode.CREATIVE) {
                return;
            }

            if (event.getPlayer().getInventory().getItem(event.getHand()).getAmount() == 1) {
                event.getPlayer().getInventory().setItem(event.getHand(), null);
            } else {
                ItemStack newStack = event.getPlayer().getInventory().getItem(event.getHand());
                newStack.setAmount(newStack.getAmount() - 1);
                event.getPlayer().getInventory().setItem(event.getHand(), newStack);
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityTarget(EntityTargetEvent event) {
        if (!stopTargetting && !mobRepellant
            || !(event.getEntity() instanceof Monster monster) || !(event.getTarget() instanceof Player)
            || !EventUtil.passesFilter(event)) {
            return;
        }

        CraftBookPlayer player = CraftBookPlugin.inst().wrapPlayer((Player) event.getTarget());

        if (stopTargetting && player.hasPermission("craftbook.betterleads.ignore-target") && monster.isLeashed()) {
            if (monster.getLeashHolder() == event.getTarget()) {
                event.setTarget(null);
                event.setCancelled(true);
                return;
            }
        }

        if (mobRepellant && player.hasPermission("craftbook.betterleads.repel-mobs")) {
            for (Entity ent : event.getTarget().getNearbyEntities(MAX_LEASH_DISTANCE, MAX_LEASH_DISTANCE, MAX_LEASH_DISTANCE)) {
                if (ent == null || !ent.isValid() || ent.getType() != event.getEntity().getType()) {
                    continue;
                }

                if (monster.isLeashed() && monster.getLeashHolder() == event.getTarget()) {
                    event.setTarget(null);
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onHitchBreak(final HangingBreakByEntityEvent event) {
        if (!persistentHitch && !ownerBreakOnly) {
            return;
        }
        if (!(event.getEntity() instanceof LeashHitch)) {
            return;
        }
        if (!(event.getRemover() instanceof Player)) {
            return;
        }
        if (!EventUtil.passesFilter(event)) {
            return;
        }

        event.setCancelled(true);

        int amountConnected = 0;

        for (Entity ent : event.getEntity().getNearbyEntities(MAX_LEASH_DISTANCE, MAX_LEASH_DISTANCE, MAX_LEASH_DISTANCE)) {
            if (!(ent instanceof LivingEntity entity)) {
                continue;
            }

            if (!entity.isLeashed() || entity.getLeashHolder() != event.getEntity()) {
                continue;
            }

            boolean canBreak = !ownerBreakOnly || !(ent instanceof Tameable);
            if (!canBreak) {
                // If we still can't break, check if it's the owner.
                if (!((Tameable) ent).isTamed() || ((Tameable) ent).getOwner() == event.getRemover()) {
                    canBreak = true;
                }
            }

            if (canBreak || event.getRemover().hasPermission("craftbook.betterleads.owner-break.bypass")) {
                entity.setLeashHolder(null);
                event.getEntity().getWorld().dropItemNaturally(event.getEntity().getLocation(), new ItemStack(Material.LEAD, 1));
            } else {
                amountConnected++;
            }
        }

        if (!persistentHitch && amountConnected == 0) {
            //Still needs to be used by further plugins in the event.
            Bukkit.getScheduler().runTask(CraftBookPlugin.inst(), event.getEntity()::remove);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onUnleash(PlayerUnleashEntityEvent event) {
        if (!ownerBreakOnly || !(event.getEntity() instanceof Tameable entity) || !EventUtil.passesFilter(event)) {
            return;
        }

        if (!entity.isLeashed() || !(entity.getLeashHolder() instanceof LeashHitch) || !entity.isTamed()) {
            return;
        }

        CraftBookPlayer player = CraftBookPlugin.inst().wrapPlayer(event.getPlayer());

        if (entity.getOwner() != event.getPlayer() && !player.hasPermission("craftbook.betterleads.owner-break.bypass")) {
            event.setCancelled(true);
        }
    }

    private boolean stopTargetting;
    private boolean ownerBreakOnly;
    private boolean persistentHitch;
    private boolean mobRepellant;
    private Set<String> allowedMobs;

    @Override
    public void loadFromConfiguration(YAMLProcessor config) {
        config.setComment("stop-mob-target", "Stop hostile mobs targeting you if you are holding them on a leash.");
        stopTargetting = config.getBoolean("stop-mob-target", false);

        config.setComment("owner-unleash-only", "Only allow the owner of tameable entities to unleash them from a leash hitch.");
        ownerBreakOnly = config.getBoolean("owner-unleash-only", false);

        config.setComment("hitch-persists", "Stop leash hitches breaking when no entities are attached. This allows for a public horse hitch or similar.");
        persistentHitch = config.getBoolean("hitch-persists", false);

        config.setComment("mob-repel", "If you have a mob tethered to you, mobs of that type will not target you.");
        mobRepellant = config.getBoolean("mob-repel", false);

        config.setComment("allowed-mobs", "The list of mobs that can be tethered with a lead.");
        allowedMobs = ImmutableSet.copyOf(config.getStringList("allowed-mobs", Lists.newArrayList(EntityTypes.ZOMBIE.getId(), EntityTypes.SPIDER.getId())));
    }
}
