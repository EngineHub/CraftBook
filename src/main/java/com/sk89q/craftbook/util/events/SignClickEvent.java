package com.sk89q.craftbook.util.events;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;

public class SignClickEvent extends PlayerInteractEvent {

    private static final HandlerList handlers = new HandlerList();

    private final ChangedSign sign;

    public SignClickEvent (Player who, Action action, ItemStack item, Block clickedBlock, BlockFace clickedFace) {
        super(who, action, item, clickedBlock, clickedFace);

        sign = BukkitUtil.toChangedSign(getClickedBlock());
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public ChangedSign getSign() {
        return sign;
    }

    public LocalPlayer getWrappedPlayer() {
        return CraftBookPlugin.inst().wrapPlayer(getPlayer());
    }
}