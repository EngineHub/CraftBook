package com.sk89q.craftbook.mechanics.pipe;

import org.bukkit.block.Block;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class PipeSuckEvent extends PipeEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private Block sucked;

    public PipeSuckEvent(Block theBlock, List<ItemStack> items, Block sucked) {
        super(theBlock, items);
        this.sucked = sucked;
    }

    public Block getSuckedBlock() {
        return sucked;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public boolean isCancelled () {
        return isCancelled;
    }

    @Override
    public void setCancelled (boolean arg0) {
        isCancelled = arg0;
    }

    private boolean isCancelled = false;

    public boolean isValid() {
        return !isCancelled && !getItems().isEmpty();
    }
}