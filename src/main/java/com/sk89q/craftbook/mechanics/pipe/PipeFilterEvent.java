package com.sk89q.craftbook.mechanics.pipe;

import org.bukkit.block.Block;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;

public class PipeFilterEvent extends PipeEvent {

    private static final HandlerList handlers = new HandlerList();

    private Set<ItemStack> filters;
    private Set<ItemStack> exceptions;
    private List<ItemStack> filteredItems;

    public PipeFilterEvent(Block theBlock, List<ItemStack> items, Set<ItemStack> filters, Set<ItemStack> exceptions, List<ItemStack> filteredItems) {
        super(theBlock, items);

        this.filters = filters;
        this.exceptions = exceptions;
        this.filteredItems = filteredItems;
    }

    public Set<ItemStack> getFilters() {
        return filters;
    }

    public Set<ItemStack> getExceptions() {
        return exceptions;
    }

    public List<ItemStack> getFilteredItems() {
        return filteredItems;
    }

    public void setFilteredItems(List<ItemStack> filteredItems) {
        this.filteredItems = filteredItems;
    }

    @Override
    @Nonnull
    public HandlerList getHandlers() {
        return handlers;
    }

    @Nonnull
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
