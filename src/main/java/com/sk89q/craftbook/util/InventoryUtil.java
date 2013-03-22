package com.sk89q.craftbook.util;

import java.util.ArrayList;
import java.util.Arrays;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Class for utilities that include adding items to a furnace based on if it is a fuel or not, and adding items to a chest. Also will include methdos for checking contents and removing.
 */
public class InventoryUtil {

    /**
     * Adds items to an inventory, returning the leftovers.
     * 
     * @param inv The inventory to add the items to.
     * @param stacks The stacks to add to the inventory.
     * @return The stacks that could not be added.
     */
    public static ArrayList<ItemStack> addItemsToInventory(Inventory inv, ItemStack ... stacks) {

        //TODO finish this (Make it call the seperate specific methods in this class.
        return null;
    }

    /**
     * Checks whether the inventory contains all the given itemstacks.
     * 
     * @param inv The inventory to check.
     * @param exact Whether the stacks need to be the exact amount.
     * @param stacks The stacks to check.
     * @return whether the inventory contains all the items. If there are no items to check, it returns true.
     */
    public boolean doesInventoryContain(Inventory inv, boolean exact, ItemStack ... stacks) {

        ArrayList<ItemStack> itemsToFind = (ArrayList<ItemStack>) Arrays.asList(stacks);

        if(itemsToFind.isEmpty())
            return true;

        for (ItemStack item : inv.getContents()) {

            if(!ItemUtil.isStackValid(item))
                continue;

            for(ItemStack base : stacks) {

                if(!itemsToFind.contains(base))
                    continue;

                if(!ItemUtil.isStackValid(base)) {
                    itemsToFind.remove(base);
                    continue;
                }

                if(ItemUtil.areItemsIdentical(base, item)) {

                    if(exact && base.getAmount() != item.getAmount())
                        continue;

                    itemsToFind.remove(base);
                    break;
                }
            }
        }

        return itemsToFind.isEmpty();
    }
}