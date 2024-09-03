package com.sk89q.craftbook.mechanics.pipe;

import org.bukkit.block.Block;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

public class PipePutEvent extends PipeEvent implements Cancellable {

    private Block put;
    private List<ItemStack> rejectedItems;

    public PipePutEvent (Block theBlock, List<ItemStack> items, Block put) {
        super(theBlock, items);
        this.put = put;
        this.rejectedItems = new ArrayList<>();
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    private static final HandlerList handlers = new HandlerList();

    public Block getPuttingBlock() {

        return put;
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

    public void applyCustomPredicate(Predicate<ItemStack> predicate) {
        for (Iterator<ItemStack> itemIterator = getItems().iterator(); itemIterator.hasNext();) {
            ItemStack item = itemIterator.next();

            if (predicate.test(item))
                continue;

            itemIterator.remove();
            rejectedItems.add(item);
        }
    }

    public List<ItemStack> getRejectedItems() {
        return Collections.unmodifiableList(this.rejectedItems);
    }
}
