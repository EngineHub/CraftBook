package com.sk89q.craftbook.circuits.ic;

import java.util.List;

import org.bukkit.inventory.ItemStack;

import com.sk89q.craftbook.circuits.Pipes;
import com.sk89q.worldedit.BlockWorldVector;

public interface PipeInputIC {

    /**
     * Called when a pipe transfers items into an {@link IC}.
     *
     * @param pipe The location of the {@link Pipes}.
     * @param items A list of {@link ItemStack}s to add to the IC.
     *
     * @return A list of {@link ItemStack}s that it could not put into the {@link IC}.
     */
    public List<ItemStack> onPipeTransfer(BlockWorldVector pipe, List<ItemStack> items);
}