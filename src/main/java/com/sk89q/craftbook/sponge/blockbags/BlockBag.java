package com.sk89q.craftbook.sponge.blockbags;

import java.util.List;

import org.spongepowered.api.item.inventory.ItemStack;

/**
 * Represents an object that contains an inventory, and can be used as a source of blocks for CraftBook mechanics.
 */
public abstract class BlockBag {

    public abstract List<ItemStack> addItems(ItemStack... itemStacks);

    public abstract List<ItemStack> removeItems(ItemStack... itemStacks);
}
