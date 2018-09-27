package com.sk89q.craftbook.mechanics.items;

import org.bukkit.Material;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

public enum ClickType {

    CLICK_LEFT,CLICK_RIGHT,CLICK_EITHER,CLICK_LEFT_BLOCK,CLICK_RIGHT_BLOCK,CLICK_EITHER_BLOCK,CLICK_LEFT_AIR,CLICK_RIGHT_AIR,CLICK_EITHER_AIR,
    ENTITY_RIGHT,ENTITY_LEFT,ENTITY_ARROW,ENTITY_PROJECTILE,ENTITY_EITHER,BLOCK_BREAK,BLOCK_PLACE,BLOCK_PROJECTILE_AIR,BLOCK_PROJECTILE_BLOCK,
    BLOCK_PROJECTILE_EITHER,PROJECTILE_LAUNCH,BLOCK_EITHER,ITEM_CONSUME,ITEM_DROP,ITEM_BREAK,ITEM_PICKUP,ITEM_CLICK_LEFT,ITEM_CLICK_RIGHT,ITEM_CLICK_EITHER,
    PLAYER_DEATH,PLAYER_CHAT,PASSIVE,ANY;

    public boolean doesPassType(Event event) {

        switch(this) {
            case ANY:
                return true;
            case PASSIVE:
                return event == null;
            case BLOCK_BREAK:
                return event instanceof BlockBreakEvent;
            case BLOCK_PLACE:
                return event instanceof BlockPlaceEvent;
            case BLOCK_PROJECTILE_EITHER:
                return event instanceof ProjectileHitEvent;
            case BLOCK_PROJECTILE_AIR:
                return event instanceof ProjectileHitEvent && ((ProjectileHitEvent) event).getEntity().getLocation().getBlock().getType() == Material.AIR;
            case BLOCK_PROJECTILE_BLOCK:
                return event instanceof ProjectileHitEvent && ((ProjectileHitEvent) event).getEntity().getLocation().getBlock().getType() != Material.AIR;
            case BLOCK_EITHER:
                return event instanceof BlockBreakEvent || event instanceof BlockPlaceEvent;
            case CLICK_EITHER:
                return event instanceof PlayerInteractEvent && ((PlayerInteractEvent) event).getAction() != Action.PHYSICAL;
            case CLICK_LEFT:
                return event instanceof PlayerInteractEvent && (((PlayerInteractEvent) event).getAction() == Action.LEFT_CLICK_AIR || ((PlayerInteractEvent) event).getAction() == Action.LEFT_CLICK_BLOCK);
            case CLICK_RIGHT:
                return event instanceof PlayerInteractEvent && (((PlayerInteractEvent) event).getAction() == Action.RIGHT_CLICK_AIR || ((PlayerInteractEvent) event).getAction() == Action.RIGHT_CLICK_BLOCK);
            case CLICK_EITHER_BLOCK:
                return event instanceof PlayerInteractEvent && (((PlayerInteractEvent) event).getAction() == Action.LEFT_CLICK_BLOCK || ((PlayerInteractEvent) event).getAction() == Action.RIGHT_CLICK_BLOCK);
            case CLICK_LEFT_BLOCK:
                return event instanceof PlayerInteractEvent && ((PlayerInteractEvent) event).getAction() == Action.LEFT_CLICK_BLOCK;
            case CLICK_RIGHT_BLOCK:
                return event instanceof PlayerInteractEvent && ((PlayerInteractEvent) event).getAction() == Action.RIGHT_CLICK_BLOCK;
            case CLICK_EITHER_AIR:
                return event instanceof PlayerInteractEvent && (((PlayerInteractEvent) event).getAction() == Action.LEFT_CLICK_AIR || ((PlayerInteractEvent) event).getAction() == Action.RIGHT_CLICK_AIR);
            case CLICK_LEFT_AIR:
                return event instanceof PlayerInteractEvent && ((PlayerInteractEvent) event).getAction() == Action.LEFT_CLICK_AIR;
            case CLICK_RIGHT_AIR:
                return event instanceof PlayerInteractEvent && ((PlayerInteractEvent) event).getAction() == Action.RIGHT_CLICK_AIR;
            case ENTITY_ARROW:
            case ENTITY_PROJECTILE:
                return event instanceof EntityDamageByEntityEvent && ((EntityDamageByEntityEvent) event).getDamager() instanceof Projectile;
            case PROJECTILE_LAUNCH:
                return event instanceof ProjectileLaunchEvent;
            case ENTITY_EITHER:
                return event instanceof PlayerInteractEntityEvent || event instanceof EntityDamageByEntityEvent;
            case ENTITY_LEFT:
                return event instanceof EntityDamageByEntityEvent;
            case ENTITY_RIGHT:
                return event instanceof PlayerInteractEntityEvent;
            case ITEM_BREAK:
                return event instanceof PlayerItemBreakEvent;
            case ITEM_CLICK_EITHER:
                return event instanceof InventoryClickEvent;
            case ITEM_CLICK_LEFT:
                return event instanceof InventoryClickEvent && ((InventoryClickEvent) event).getClick().isLeftClick();
            case ITEM_CLICK_RIGHT:
                return event instanceof InventoryClickEvent && ((InventoryClickEvent) event).getClick().isRightClick();
            case ITEM_CONSUME:
                return event instanceof PlayerItemConsumeEvent;
            case ITEM_DROP:
                return event instanceof PlayerDropItemEvent;
            case ITEM_PICKUP:
                return event instanceof EntityPickupItemEvent;
            case PLAYER_CHAT:
                return event instanceof AsyncPlayerChatEvent;
            case PLAYER_DEATH:
                return event instanceof PlayerDeathEvent;
            default:
                break;
        }

        return false;
    }
}