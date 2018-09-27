package com.sk89q.craftbook.mechanics.pipe;

import java.util.List;

import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public class PipeFinishEvent extends PipeEvent {

    private Block origin;

    private boolean request;

    public PipeFinishEvent (Block theBlock, List<ItemStack> items, Block origin, boolean request) {
        super(theBlock, items);
        this.origin = origin;
        this.request = request;
    }

    public Block getOrigin() {
        return origin;
    }

    public boolean isRequest() {
        return request;
    }
}