package com.sk89q.craftbook.sponge.blockbags;

import org.spongepowered.api.item.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class AdminBlockBag extends BlockBag {
    @Override
    public boolean has(List<ItemStack> itemStacks) {
        return true;
    }

    @Override
    public List<ItemStack> add(List<ItemStack> itemStacks) {
        return new ArrayList<>();
    }

    @Override
    public List<ItemStack> remove(List<ItemStack> itemStacks) {
        return new ArrayList<>();
    }
}
