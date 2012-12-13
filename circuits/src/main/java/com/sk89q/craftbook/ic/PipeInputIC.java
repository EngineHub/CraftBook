package com.sk89q.craftbook.ic;

import java.util.List;

import org.bukkit.inventory.ItemStack;

import com.sk89q.worldedit.BlockWorldVector;

public interface PipeInputIC {

    /**
     * Called when a pipe transfers items into an IC.
     * 
     * @param pipe
     * @param items
     * @return A list of items that it could not put into the IC.
     */
    public List<ItemStack> onPipeTransfer(BlockWorldVector pipe, List<ItemStack> items);
}