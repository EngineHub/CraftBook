package com.sk89q.craftbook.sponge.blockbags;

import java.util.Arrays;
import java.util.List;

import org.spongepowered.api.item.inventory.ItemStack;

/**
 * An interface for accessing multiple block bags at the same time.
 */
public class BlockBagInterface extends BlockBag {

    BlockBag[] bags;

    public BlockBagInterface(BlockBag ... bags) {
        this.bags = bags;
    }

    @Override
    public List<ItemStack> addItems(ItemStack... itemStacks) {
        for(BlockBag bag : bags) {
            if(itemStacks.length == 0) break;
            itemStacks = bag.addItems(itemStacks).toArray(new ItemStack[0]);
        }

        return Arrays.asList(itemStacks);
    }

    @Override
    public List<ItemStack> removeItems(ItemStack... itemStacks) {
        for(BlockBag bag : bags) {
            if(itemStacks.length == 0) break;
            itemStacks = bag.removeItems(itemStacks).toArray(new ItemStack[0]);
        }

        return Arrays.asList(itemStacks);
    }

}
