package com.sk89q.craftbook.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.block.BlockState;
import org.bukkit.block.BrewingStand;
import org.bukkit.block.Furnace;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

/**
 * Class for utilities that include adding items to a furnace based on if it is a fuel or not, and adding items to a chest. Also will include methdos for checking contents and removing.
 */
public class InventoryUtil {

    /**
     * Adds items to an inventory, returning the leftovers.
     * 
     * @param container The InventoryHolder to add the items to.
     * @param stacks The stacks to add to the inventory.
     * @return The stacks that could not be added.
     */
    public static ArrayList<ItemStack> addItemsToInventory(InventoryHolder container, ItemStack ... stacks) {

        if(container instanceof Furnace) {
            return addItemsToFurnace((Furnace) container, stacks);
        } else if(container instanceof BrewingStand) {
            return addItemsToBrewingStand((BrewingStand) container, stacks);
        } else { //Basic inventories like chests, dispensers, storage carts, etc.
            ArrayList<ItemStack> leftovers = new ArrayList<ItemStack>(container.getInventory().addItem(stacks).values());
            if(container instanceof BlockState)
                ((BlockState) container).update();
            return leftovers;
        }
    }

    /**
     * Adds items to a furnace, returning the leftovers.
     * 
     * @param furnace The Furnace to add the items to.
     * @param stacks The stacks to add to the inventory.
     * @return The stacks that could not be added.
     */
    public static ArrayList<ItemStack> addItemsToFurnace(Furnace furnace, ItemStack ... stacks) {

        ArrayList<ItemStack> leftovers = new ArrayList<ItemStack>();

        for(ItemStack stack : stacks) {

            if(!ItemUtil.isStackValid(stack))
                continue;

            if (ItemUtil.isFurnacable(stack) && fitsInSlot(stack, furnace.getInventory().getSmelting())) {
                if (furnace.getInventory().getSmelting() == null)
                    furnace.getInventory().setSmelting(stack);
                else
                    leftovers.add(ItemUtil.addToStack(furnace.getInventory().getSmelting(), stack));
            } else if (ItemUtil.isAFuel(stack) && fitsInSlot(stack, furnace.getInventory().getFuel())) {
                if (furnace.getInventory().getFuel() == null)
                    furnace.getInventory().setFuel(stack);
                else
                    leftovers.add(ItemUtil.addToStack(furnace.getInventory().getFuel(), stack));
            } else {
                leftovers.add(stack);
            }
        }
        while(leftovers.remove(null)){}

        furnace.update();

        return leftovers;
    }

    /**
     * Adds items to a BrewingStand, returning the leftovers.
     * 
     * @param brewingStand The BrewingStand to add the items to.
     * @param stacks The stacks to add to the inventory.
     * @return The stacks that could not be added.
     */
    public static ArrayList<ItemStack> addItemsToBrewingStand(BrewingStand brewingStand, ItemStack ... stacks) {

        ArrayList<ItemStack> leftovers = new ArrayList<ItemStack>();

        for(ItemStack stack : stacks) {

            if (!ItemUtil.isAPotionIngredient(stack)) {
                leftovers.add(stack);
                continue;
            }
            BrewerInventory inv = brewingStand.getInventory();
            if (InventoryUtil.fitsInSlot(stack, inv.getIngredient())) {
                if (inv.getIngredient() == null) {
                    inv.setIngredient(stack);
                } else {
                    leftovers.add(ItemUtil.addToStack(inv.getIngredient(), stack));
                }
            } else {
                leftovers.add(stack);
            }
        }

        brewingStand.update();

        return leftovers;
    }

    /**
     * Checks whether the inventory contains all the given itemstacks.
     * 
     * @param inv The inventory to check.
     * @param exact Whether the stacks need to be the exact amount.
     * @param stacks The stacks to check.
     * @return whether the inventory contains all the items. If there are no items to check, it returns true.
     */
    public static boolean doesInventoryContain(Inventory inv, boolean exact, ItemStack ... stacks) {

        ArrayList<ItemStack> itemsToFind = new ArrayList<ItemStack>(Arrays.asList(stacks));

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

    /**
     * Removes items from an inventory.
     * 
     * @param inv The inventory to remove it from.
     * @param stacks The stacks to remove.
     * @return Whether the stacks were removed.
     */
    public static boolean removeItemsFromInventory(InventoryHolder inv, ItemStack ... stacks) {

        List<ItemStack> leftovers = new ArrayList<ItemStack>(inv.getInventory().removeItem(stacks).values());

        if(!leftovers.isEmpty()) {
            List<ItemStack> itemsToAdd = new ArrayList<ItemStack>(Arrays.asList(stacks));
            for(ItemStack left : leftovers) {
                itemsToAdd.remove(left);
            }

            inv.getInventory().addItem(itemsToAdd.toArray(new ItemStack[itemsToAdd.size()]));
        }

        if(inv instanceof BlockState)
            ((BlockState) inv).update();

        return leftovers.isEmpty();
    }

    /**
     * Checks whether the itemstack can easily stack onto the other itemstack.
     * 
     * @param stack The stack to add.
     * @param slot The base stack.
     * @return whether it can be added or not.
     */
    public static boolean fitsInSlot(ItemStack stack, ItemStack slot) {

        return slot == null || ItemUtil.areItemsIdentical(stack, slot) && stack.getAmount() + slot.getAmount() <= stack.getMaxStackSize();
    }
}