package com.sk89q.craftbook.sponge.blockbags;

import org.spongepowered.api.item.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

/**
 * An interface for accessing multiple block bags at the same time.
 */
public class BlockBagInterface extends BlockBag {

    BlockBag[] bags;

    public BlockBagInterface(BlockBag... bags) {
        this.bags = bags;
    }

    @Override
    public List<ItemStack> addItems(ItemStack... itemStacks) {
        for (BlockBag bag : bags) {
            if (itemStacks.length == 0) break;
            List<ItemStack> stacks = bag.addItems(itemStacks);
            itemStacks = stacks.toArray(new ItemStack[stacks.size()]);
        }

        return Arrays.asList(itemStacks);
    }

    @Override
    public List<ItemStack> removeItems(ItemStack... itemStacks) {
        for (BlockBag bag : bags) {
            if (itemStacks.length == 0) break;
            List<ItemStack> stacks = bag.removeItems(itemStacks);
            itemStacks = stacks.toArray(new ItemStack[stacks.size()]);
        }

        return Arrays.asList(itemStacks);
    }

}
