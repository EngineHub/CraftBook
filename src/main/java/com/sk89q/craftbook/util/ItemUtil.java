package com.sk89q.craftbook.util;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Item;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.blocks.ItemID;

public class ItemUtil {

    private ItemUtil() {

    }

    /**
     * Add an itemstack to an existing itemstack.
     * 
     * @param base The itemstack to be added to.
     * @param toAdd The itemstack to add to the base.
     * @return The unaddable items.
     */
    public static ItemStack addToStack(ItemStack base, ItemStack toAdd) {

        if (!areItemsIdentical(base, toAdd)) return toAdd;

        if (base.getAmount() + toAdd.getAmount() > 64) {

            toAdd.setAmount(base.getAmount() + toAdd.getAmount() - 64);
            base.setAmount(64);
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
     * @param include The list of items to include, skipped if empty.
     * @param exclude The list of items to exclude, skipped if empty.
     * @return The list of items that have been filtered.
     */
    public static List<ItemStack> filterItems(List<ItemStack> stacks, List<ItemStack> include, List<ItemStack> exclude) {

        List<ItemStack> ret = new ArrayList<ItemStack>();

        for(ItemStack item : stacks) {

            checks: {
            if(include.size() > 0) {
                boolean passes = false;
                for(ItemStack inc : include) {

                    if(!ItemUtil.isStackValid(inc))
                        continue;
                    if(!areItemsIdentical(item, inc))
                        passes = false;
                    else {
                        passes = true;
                        break;
                    }
                }
                if(!passes)
                    break checks;
            }
            if(exclude.size() > 0) {
                for(ItemStack inc : exclude) {

                    if(!ItemUtil.isStackValid(inc))
                        continue;
                    if(areItemsIdentical(item, inc))
                        break checks;
                }
            }

            ret.add(item.clone());
        }
        }

        return ret;
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

    public static boolean areItemsIdentical(ItemStack item, int type, short data) {

        return areItemsIdentical(item, new MaterialData(type, (byte) data));
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

        return data.getItemTypeId() == comparedData.getItemTypeId() && data.getData() == data.getData();
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

        return getCookedResult(item) != null && !item.getItemMeta().hasDisplayName();
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

    public static boolean isAFuel(ItemStack item) {

        int i = item.getTypeId();
        return i == ItemID.COAL || i == BlockID.LOG || i == BlockID.WOOD || i == BlockID.WOODEN_STEP || i == BlockID
                .SAPLING || i == ItemID.WOOD_AXE
                || i == ItemID.WOOD_HOE || i == ItemID.WOOD_PICKAXE || i == ItemID.WOOD_SHOVEL || i == ItemID.WOOD_SWORD
                || i == BlockID.WOODEN_PRESSURE_PLATE || i == ItemID.STICK || i == BlockID.FENCE || i == BlockID
                .WOODEN_STAIRS
                || i == BlockID.TRAP_DOOR || i == BlockID.WORKBENCH || i == BlockID.CHEST || i == BlockID.JUKEBOX ||
                i == BlockID.NOTE_BLOCK
                || i == BlockID.BROWN_MUSHROOM_CAP || i == BlockID.RED_MUSHROOM_CAP || i == ItemID.BLAZE_ROD || i ==
                ItemID.LAVA_BUCKET;
    }

    public static boolean isAPotionIngredient(ItemStack item) {

        int i = item.getTypeId();
        return i == ItemID.NETHER_WART_SEED || i == ItemID.LIGHTSTONE_DUST || i == ItemID.REDSTONE_DUST || i ==
                ItemID.SPIDER_EYE
                || i == ItemID.MAGMA_CREAM || i == ItemID.SUGAR || i == ItemID.GLISTERING_MELON || i == ItemID
                .GHAST_TEAR || i == ItemID.BLAZE_POWDER
                || i == ItemID.FERMENTED_SPIDER_EYE || i == ItemID.SULPHUR;
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
}