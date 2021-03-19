package com.sk89q.craftbook.util;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BrewingStand;
import org.bukkit.block.Chest;
import org.bukkit.block.Furnace;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
    public static List<ItemStack> addItemsToInventory(InventoryHolder container, ItemStack ... stacks) {
        return addItemsToInventory(container, true, stacks);
    }

    /**
     * Adds items to an inventory, returning the leftovers.
     *
     * @param container The InventoryHolder to add the items to.
     * @param stacks The stacks to add to the inventory.
     * @return The stacks that could not be added.
     */
    public static List<ItemStack> addItemsToInventory(InventoryHolder container, boolean update, ItemStack ... stacks) {

        if(container instanceof Furnace) {
            return addItemsToFurnace((Furnace) container, stacks);
        } else if(container instanceof BrewingStand) {
            return addItemsToBrewingStand((BrewingStand) container, stacks);
        } else { //Basic inventories like chests, dispensers, storage carts, etc.
            List<ItemStack> leftovers = new ArrayList<>();
            if (container instanceof ShulkerBox) {
                Arrays.stream(stacks).filter(item -> ItemUtil.isShulkerBox(item.getType())).forEach(leftovers::add);
                stacks = Arrays.stream(stacks).filter(item -> !ItemUtil.isShulkerBox(item.getType())).toArray(ItemStack[]::new);
            }
            leftovers.addAll(container.getInventory().addItem(stacks).values());
            if (container.getInventory() instanceof DoubleChestInventory) {
                ((Chest) ((DoubleChestInventory) container.getInventory()).getLeftSide().getHolder()).update(true);
                ((Chest) ((DoubleChestInventory) container.getInventory()).getRightSide().getHolder()).update(true);
            }
            //if(container instanceof BlockState && update)
            //    ((BlockState) container).update();
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
    public static List<ItemStack> addItemsToFurnace(Furnace furnace, ItemStack ... stacks) {

        List<ItemStack> leftovers = new ArrayList<>();

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
        leftovers.removeAll(Collections.singleton(null));

        //furnace.update();

        return leftovers;
    }

    /**
     * Adds items to a BrewingStand, returning the leftovers.
     * 
     * @param brewingStand The BrewingStand to add the items to.
     * @param stacks The stacks to add to the inventory.
     * @return The stacks that could not be added.
     */
    public static List<ItemStack> addItemsToBrewingStand(BrewingStand brewingStand, ItemStack ... stacks) {

        List<ItemStack> leftovers = new ArrayList<>();

        for(ItemStack stack : stacks) {
            BrewerInventory inv = brewingStand.getInventory();
            if (ItemUtil.isAPotionIngredient(stack) && InventoryUtil.fitsInSlot(stack, inv.getIngredient())) {
                if (inv.getIngredient() == null) {
                    inv.setIngredient(stack);
                } else {
                    leftovers.add(ItemUtil.addToStack(inv.getIngredient(), stack));
                }
            } else if (stack.getType() == Material.BLAZE_POWDER && InventoryUtil.fitsInSlot(stack, inv.getFuel())) {
                if (inv.getFuel() == null) {
                    inv.setFuel(stack);
                } else {
                    leftovers.add(ItemUtil.addToStack(inv.getFuel(), stack));
                }
            } else if (stack.getType() == Material.GLASS_BOTTLE
                    || stack.getType() == Material.POTION
                    || stack.getType() == Material.LINGERING_POTION
                    || stack.getType() == Material.SPLASH_POTION) {
                for (int i = 0; i < 3; i++) {
                    if (stack == null) {
                        break;
                    }
                    if (inv.getItem(i) == null) {
                        inv.setItem(i, stack);
                        stack = null;
                    } else {
                        stack = ItemUtil.addToStack(inv.getItem(i), stack);
                    }
                }
                if (stack != null) {
                    leftovers.add(stack);
                }
            } else {
                leftovers.add(stack);
            }
        }

        //brewingStand.update();

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
        return doesInventoryContain(inv, !exact, false, false, false, stacks);
    }

    /**
     * Checks whether the inventory contains all the given itemstacks.
     *
     * @param inv The inventory to check.
     * @param ignoreStackSize Whether to ignore stack size count.
     * @param ignoreDurability Whether to ignore durability if damageable.
     * @param ignoreMeta Whether to ignore meta/nbt data.
     * @param ignoreEnchants Whether to ignore enchantment data.
     * @param stacks The stacks to check.
     * @return whether the inventory contains all the items. If there are no items to check, it returns true.
     */
    public static boolean doesInventoryContain(Inventory inv, boolean ignoreStackSize, boolean ignoreDurability, boolean ignoreMeta, boolean ignoreEnchants, ItemStack ... stacks) {

        ArrayList<ItemStack> itemsToFind = new ArrayList<>(Arrays.asList(stacks));

        if(itemsToFind.isEmpty())
            return true;

        List<ItemStack> items = new ArrayList<>(Arrays.asList(inv.getContents()));
        if (inv instanceof PlayerInventory) {
            items.addAll(Arrays.asList(((PlayerInventory) inv).getArmorContents()));
            items.add(((PlayerInventory) inv).getItemInOffHand());
        }

        for (ItemStack item : items) {
            if(!ItemUtil.isStackValid(item))
                continue;

            for(ItemStack base : stacks) {
                if(!itemsToFind.contains(base))
                    continue;

                if(!ItemUtil.isStackValid(base)) {
                    itemsToFind.remove(base);
                    continue;
                }

                if(ItemUtil.areItemsSimilar(base, item)) {
                    if(!ignoreStackSize && base.getAmount() != item.getAmount())
                        continue;

                    if(!ignoreDurability && (base.getType().getMaxDurability() > 0 || item.getType().getMaxDurability() > 0) && base.getDurability() != item.getDurability())
                        continue;

                    if(!ignoreMeta) {
                        if(base.hasItemMeta() != item.hasItemMeta()) {
                            if(!ignoreEnchants)
                                continue;
                            if(base.hasItemMeta() && ItemUtil.hasDisplayNameOrLore(base))
                                continue;
                            else if(item.hasItemMeta() && ItemUtil.hasDisplayNameOrLore(item))
                                continue;
                        } else if(base.hasItemMeta()) {
                            if(base.hasItemMeta() && !ItemUtil.areItemMetaIdentical(base.getItemMeta(), item.getItemMeta(), !ignoreEnchants))
                                continue;
                        }
                    }

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

        List<ItemStack> leftovers = new ArrayList<>(inv.getInventory().removeItem(stacks).values());

        if(!leftovers.isEmpty()) {
            List<ItemStack> itemsToAdd = new ArrayList<>(Arrays.asList(stacks));
            itemsToAdd.removeAll(leftovers);

            inv.getInventory().addItem(itemsToAdd.toArray(new ItemStack[itemsToAdd.size()]));
        }

        //if(inv instanceof BlockState)
        //    ((BlockState) inv).update();

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

    /**
     * Checks whether the block has an inventory.
     * 
     * @param block The block.
     * @return If it has an inventory.
     */
    public static boolean doesBlockHaveInventory(Block block) {

        switch(block.getType()) {
            case CHEST:
            case TRAPPED_CHEST:
            case DROPPER:
            case DISPENSER:
            case FURNACE:
            case BREWING_STAND:
            case HOPPER:
            case WHITE_SHULKER_BOX:
            case ORANGE_SHULKER_BOX:
            case MAGENTA_SHULKER_BOX:
            case LIGHT_BLUE_SHULKER_BOX:
            case YELLOW_SHULKER_BOX:
            case GREEN_SHULKER_BOX:
            case PINK_SHULKER_BOX:
            case GRAY_SHULKER_BOX:
            case LIGHT_GRAY_SHULKER_BOX:
            case BLUE_SHULKER_BOX:
            case PURPLE_SHULKER_BOX:
            case CYAN_SHULKER_BOX:
            case BROWN_SHULKER_BOX:
            case LIME_SHULKER_BOX:
            case BLACK_SHULKER_BOX:
            case RED_SHULKER_BOX:
            case SHULKER_BOX:
            case BLAST_FURNACE:
            case SMOKER:
            case BARREL:
                return true;
            default:
                return false;
        }
    }

    public static ItemStack getItemInHand(Player player, EquipmentSlot slot) {
        if (slot == EquipmentSlot.HAND) {
            return player.getInventory().getItemInMainHand();
        } else if (slot == EquipmentSlot.OFF_HAND) {
            return player.getInventory().getItemInOffHand();
        }

        return null;
    }

    public static void setItemInHand(Player player, EquipmentSlot slot, ItemStack itemStack) {
        if (slot == EquipmentSlot.HAND) {
            player.getInventory().setItemInMainHand(itemStack);
        } else if (slot == EquipmentSlot.OFF_HAND) {
            player.getInventory().setItemInOffHand(itemStack);
        }
    }
}
