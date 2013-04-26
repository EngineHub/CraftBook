package com.sk89q.craftbook.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.bukkit.entity.Item;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.blocks.BlockType;
import com.sk89q.worldedit.blocks.ItemID;
import com.sk89q.worldedit.blocks.ItemType;

public class ItemUtil {

    /**
     * Add an itemstack to an existing itemstack.
     * 
     * @param base The itemstack to be added to.
     * @param toAdd The itemstack to add to the base.
     * @return The unaddable items.
     */
    public static ItemStack addToStack(ItemStack base, ItemStack toAdd) {

        if (!areItemsIdentical(base, toAdd)) return toAdd;

        if (base.getAmount() + toAdd.getAmount() > base.getMaxStackSize()) {

            toAdd.setAmount(base.getAmount() + toAdd.getAmount() - base.getMaxStackSize());
            base.setAmount(base.getMaxStackSize());
            return toAdd;
        } else {
            base.setAmount(base.getAmount() + toAdd.getAmount());
            return null;
        }
    }

    /**
     * Filter a list of items by inclusions and exclusions.
     * 
     * @param stacks The base list of items.
     * @param inclusions The list of items to include, skipped if empty.
     * @param exclusions The list of items to exclude, skipped if empty.
     * @return The list of items that have been filtered.
     */
    public static List<ItemStack> filterItems(List<ItemStack> stacks, HashSet<ItemStack> inclusions, HashSet<ItemStack> exclusions) {

        List<ItemStack> ret = new ArrayList<ItemStack>();

        for(ItemStack stack : stacks) {

            if(doesItemPassFilters(stack, inclusions, exclusions))
                ret.add(stack);
        }

        return ret;
    }

    /**
     * Check whether or not an item passes filters.
     * 
     * @param stacks The item to check if it passes.
     * @param inclusions The list of items to include, skipped if empty.
     * @param exclusions The list of items to exclude, skipped if empty.
     * @return If the item passes the filters.
     */
    public static boolean doesItemPassFilters(ItemStack stack, HashSet<ItemStack> inclusions, HashSet<ItemStack> exclusions) {

        boolean passesFilters = true;
        if(inclusions.size() > 0) {
            for (ItemStack fil : inclusions) {

                if(!ItemUtil.isStackValid(fil))
                    continue;
                passesFilters = false;
                if(ItemUtil.areItemsIdentical(fil, stack)) {
                    passesFilters = true;
                    break;
                }
            }
            if(!passesFilters)
                return false;
        }
        if(exclusions.size() > 0) {
            for (ItemStack fil : exclusions) {

                if(!ItemUtil.isStackValid(fil))
                    continue;
                if(ItemUtil.areItemsIdentical(fil, stack)) {
                    passesFilters = false;
                    break;
                }
            }
            if(!passesFilters)
                return false;
        }

        return passesFilters;
    }

    public static ItemStack[] removeNulls(ItemStack[] array) {

        List<ItemStack> list = new ArrayList<ItemStack>();
        for(ItemStack t : array)
            if(t != null)
                list.add(t);

        return list.toArray(new ItemStack[list.size()]).clone();
    }

    public static boolean areItemsSimilar(ItemStack item, int type) {

        return areItemsSimilar(item, new MaterialData(type, (byte) 0));
    }

    public static boolean areItemsSimilar(ItemStack item, MaterialData data) {

        return areItemsSimilar(item.getData(), data);
    }

    public static boolean areItemsSimilar(ItemStack item, ItemStack item2) {

        return areItemsSimilar(item.getData(), item2.getData());
    }

    public static boolean areItemsSimilar(MaterialData data, MaterialData comparedData) {

        return data.getItemTypeId() == comparedData.getItemTypeId();
    }

    public static boolean areItemsIdentical(ItemStack item, int type, byte data) {

        return areItemsIdentical(item, new MaterialData(type, data));
    }

    public static boolean areItemsIdentical(ItemStack item, MaterialData data) {

        return areItemsIdentical(item.getData(), data);
    }

    public static boolean areItemsIdentical(ItemStack item, ItemStack item2) {

        if(!isStackValid(item) || !isStackValid(item2))
            return !isStackValid(item) && !isStackValid(item2);
        else
            return areItemsIdentical(item.getData(), item2.getData());
    }

    public static boolean areItemsIdentical(MaterialData data, MaterialData comparedData) {

        return data.getItemTypeId() == comparedData.getItemTypeId() && (data.getData() == comparedData.getData() || data.getData() < 0 || comparedData.getData() < 0);
    }

    public static void setItemTypeAndData(ItemStack item, int type, byte data) {

        item.setData(new MaterialData(type, data));
    }

    public static boolean isStackValid(ItemStack item) {

        return item != null && item.getAmount() > 0 && item.getTypeId() > 0;
    }

    public static boolean takeFromEntity(Item item) {

        if (item == null || item.isDead()) return false;

        if (!isStackValid(item.getItemStack())) {
            item.remove();
            return false;
        }

        item.getItemStack().setAmount(item.getItemStack().getAmount() - 1);

        if (!isStackValid(item.getItemStack())) {
            item.remove();
        }

        return true;
    }

    public static boolean isCookable(ItemStack item) {

        return getCookedResult(item) != null && !item.hasItemMeta();
    }

    public static ItemStack getCookedResult(ItemStack item) {

        switch (item.getTypeId()) {
            case ItemID.RAW_BEEF:
                return new ItemStack(ItemID.COOKED_BEEF);
            case ItemID.RAW_CHICKEN:
                return new ItemStack(ItemID.COOKED_CHICKEN);
            case ItemID.RAW_FISH:
                return new ItemStack(ItemID.COOKED_FISH);
            case ItemID.RAW_PORKCHOP:
                return new ItemStack(ItemID.COOKED_PORKCHOP);
            case ItemID.POTATO:
                return new ItemStack(ItemID.BAKED_POTATO);
            default:
                return null;
        }
    }

    public static boolean isSmeltable(ItemStack item) {

        return getSmeletedResult(item) != null && !item.getItemMeta().hasDisplayName();
    }

    public static ItemStack getSmeletedResult(ItemStack item) {

        switch (item.getTypeId()) {
            case BlockID.COBBLESTONE:
                return new ItemStack(BlockID.STONE);
            case BlockID.CACTUS:
                return new ItemStack(ItemID.INK_SACK, 1, (short) 2);
            case BlockID.LOG:
                return new ItemStack(ItemID.COAL, 1, (short) 1);
            case BlockID.IRON_ORE:
                return new ItemStack(ItemID.IRON_BAR);
            case BlockID.REDSTONE_ORE:
                return new ItemStack(ItemID.REDSTONE_DUST);
            case BlockID.EMERALD_ORE:
                return new ItemStack(ItemID.EMERALD);
            case BlockID.GOLD_ORE:
                return new ItemStack(ItemID.GOLD_BAR);
            case BlockID.DIAMOND_ORE:
                return new ItemStack(ItemID.DIAMOND);
            case BlockID.SAND:
                return new ItemStack(BlockID.GLASS);
            case ItemID.CLAY_BALL:
                return new ItemStack(ItemID.BRICK_BAR);
            case BlockID.NETHERRACK:
                return new ItemStack(ItemID.NETHER_BRICK);
            default:
                return null;
        }
    }

    /**
     * Checks whether the item is usable as a fuel in a furnace.
     * 
     * @param item The item to check.
     * @return Whether it is usable in a furnace.
     */
    public static boolean isAFuel(ItemStack item) {

        switch(item.getTypeId()) {
            case ItemID.COAL:
            case BlockID.LOG:
            case BlockID.WOOD:
            case BlockID.WOODEN_STEP:
            case BlockID.SAPLING:
            case ItemID.WOOD_AXE:
            case ItemID.WOOD_HOE:
            case ItemID.WOOD_PICKAXE:
            case ItemID.WOOD_SHOVEL:
            case ItemID.WOOD_SWORD:
            case BlockID.WOODEN_PRESSURE_PLATE:
            case ItemID.STICK:
            case BlockID.FENCE:
            case BlockID.FENCE_GATE:
            case BlockID.WOODEN_STAIRS:
            case BlockID.TRAP_DOOR:
            case BlockID.WORKBENCH:
            case BlockID.CHEST:
            case BlockID.TRAPPED_CHEST:
            case BlockID.JUKEBOX:
            case BlockID.NOTE_BLOCK:
            case BlockID.BROWN_MUSHROOM_CAP:
            case BlockID.RED_MUSHROOM_CAP:
            case ItemID.BLAZE_ROD:
            case ItemID.LAVA_BUCKET:
            case BlockID.BOOKCASE:
                return true;
            default:
                return false;
        }
    }

    /**
     * Checks whether an item is a potion ingredient.
     * 
     * @param item The item to check.
     * @return If the item is a potion ingredient.
     */
    public static boolean isAPotionIngredient(ItemStack item) {

        switch(item.getTypeId()) {
            case ItemID.NETHER_WART_SEED:
            case ItemID.LIGHTSTONE_DUST:
            case ItemID.REDSTONE_DUST:
            case ItemID.SPIDER_EYE:
            case ItemID.MAGMA_CREAM:
            case ItemID.SUGAR:
            case ItemID.GLISTERING_MELON:
            case ItemID.GHAST_TEAR:
            case ItemID.BLAZE_POWDER:
            case ItemID.FERMENTED_SPIDER_EYE:
            case ItemID.SULPHUR:
            case ItemID.GOLDEN_CARROT:
                return true;
            default:
                return false;
        }
    }

    public static boolean containsRawFood(Inventory inv) {

        for (ItemStack it : inv.getContents()) { if (it != null && isCookable(it)) return true; }
        return false;
    }

    public static boolean containsRawMinerals(Inventory inv) {

        for (ItemStack it : inv.getContents()) { if (it != null && isSmeltable(it)) return true; }
        return false;
    }

    public static boolean isFurnacable(ItemStack item) {

        return isCookable(item) || isSmeltable(item);
    }

    public static boolean isItemEdible(ItemStack item) {

        return item.getType().isEdible();
    }

    public static ItemStack getUsedItem(ItemStack item) {

        if (item.getTypeId() == ItemID.MUSHROOM_SOUP) {
            item.setTypeId(ItemID.BOWL); // Get your bowl back
        } else if (item.getTypeId() == ItemID.POTION) {
            item.setTypeId(ItemID.GLASS_BOTTLE); // Get your bottle back
        } else if (item.getTypeId() == ItemID.LAVA_BUCKET || item.getTypeId() == ItemID.WATER_BUCKET || item
                .getTypeId() == ItemID.MILK_BUCKET) {
            item.setTypeId(ItemID.BUCKET); // Get your bucket back
        } else if (item.getAmount() == 1) {
            item.setTypeId(0);
        } else {
            item.setAmount(item.getAmount() - 1);
        }
        return item;
    }

    public static ItemStack getSmallestStackOfType(ItemStack[] stacks, ItemStack item) {

        ItemStack smallest = null;
        for (ItemStack it : stacks) {
            if (!ItemUtil.isStackValid(it)) {
                continue;
            }
            if (ItemUtil.areItemsIdentical(it, item)) {
                if (smallest == null) {
                    smallest = it;
                }
                if (it.getAmount() < smallest.getAmount()) {
                    smallest = it;
                }
            }
        }

        return smallest;
    }

    /**
     * Parse an item from a line of text.
     * 
     * @param line The line to parse it from.
     * @return The item to create.
     */
    public static ItemStack getItem(String line) {

        if (line == null || line.isEmpty())
            return null;

        int id = 0;
        int data = -1;
        int amount = 1;

        String[] amountSplit = RegexUtil.ASTERISK_PATTERN.split(line, 2);
        String[] dataSplit = RegexUtil.COLON_PATTERN.split(amountSplit[0], 2);
        try {
            id = Integer.parseInt(dataSplit[0]);
        } catch (NumberFormatException e) {
            try {
                id = BlockType.lookup(dataSplit[0]).getID();
                if (id < 0) id = 0;
            } catch (Exception ee) {
                try {
                    id = ItemType.lookup(dataSplit[0]).getID();
                }
                catch(Exception eee){}
            }
        }
        try {
            if (dataSplit.length > 1)
                data = Integer.parseInt(dataSplit[1]);
        }
        catch(Exception e){}
        try {
            if(amountSplit.length > 1)
                amount = Integer.parseInt(amountSplit[1]);
        }
        catch(Exception e){}

        ItemStack rVal = new ItemStack(id, amount, (short) data);
        rVal.setData(new MaterialData(id, (byte)data));
        return rVal;
    }
}