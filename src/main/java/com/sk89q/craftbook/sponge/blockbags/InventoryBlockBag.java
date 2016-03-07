package com.sk89q.craftbook.sponge.blockbags;

import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.transaction.InventoryTransactionResult;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class InventoryBlockBag extends BlockBag {

    Inventory inventory;

    public InventoryBlockBag(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public boolean has(List<ItemStack> itemStacks) {
        for(ItemStack stack : itemStacks) {
            if(!inventory.contains(stack))
                return false;
        }
        return true;
    }

    @Override
    public List<ItemStack> add(List<ItemStack> itemStacks) {
        List<ItemStack> output = new ArrayList<>();
        for(ItemStack stack : itemStacks) {
            InventoryTransactionResult result = inventory.offer(stack);
            if(result.getRejectedItems().size() > 0) {
                output.addAll(result.getRejectedItems().stream().map(ItemStackSnapshot::createStack).collect(Collectors.toList()));
            }
        }
        return output;
    }

    @Override
    public List<ItemStack> remove(List<ItemStack> itemStacks) {
        /*List<ItemStack> output = new ArrayList<>();
        for(ItemStack stack : itemStacks) {
            InventoryTransactionResult result = carrier.getInventory().(stack);
            if(result.getRejectedItems().size() > 0) {
                output.addAll(result.getRejectedItems().stream().map(ItemStackSnapshot::createStack).collect(Collectors.toList()));
            }
        }
        return output;*/
        //TODO
        return itemStacks;
    }
}
