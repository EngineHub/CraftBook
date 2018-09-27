package com.sk89q.craftbook.mechanics.pipe;

import java.util.List;

import org.bukkit.block.Block;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

public class PipeRequestEvent extends PipeSuckEvent {

    private static final HandlerList handlers = new HandlerList();

    public PipeRequestEvent (Block theBlock, List<ItemStack> items, Block sucked) {
        super(theBlock, items, sucked);
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}