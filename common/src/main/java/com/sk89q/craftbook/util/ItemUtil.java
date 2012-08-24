package com.sk89q.craftbook.util;

import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.blocks.ItemID;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

public class ItemUtil {

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

    public static boolean isCookable(ItemStack item) {

        return getCookedResult(item) != null;
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
            default:
                return null;
        }
    }

    public static boolean isSmeltable(ItemStack item) {

        return getSmeletedResult(item) != null;
    }

    public static ItemStack getSmeletedResult(ItemStack item) {

        switch (item.getTypeId()) {
            case BlockID.IRON_ORE:
                return new ItemStack(ItemID.IRON_BAR);
            case BlockID.GOLD_ORE:
                return new ItemStack(ItemID.GOLD_BAR);
            case BlockID.DIAMOND_ORE:
                return new ItemStack(ItemID.DIAMOND);
            case BlockID.SAND:
                return new ItemStack(BlockID.GLASS);
            case ItemID.CLAY_BALL:
                return new ItemStack(ItemID.BRICK_BAR);
            default:
                return null;
        }
    }

    public static boolean containsRawFood(Inventory inv) {

        for (ItemStack it : inv.getContents())
            if (it != null && isCookable(it)) return true;
        return false;
    }

    public static boolean containsRawMinerals(Inventory inv) {

        for (ItemStack it : inv.getContents())
            if (it != null && isSmeltable(it)) return true;
        return false;
    }

    public static boolean isItemEdible(ItemStack item) {

        return item.getType().isEdible();
    }

    public static ItemStack getUsedItem(ItemStack item) {

        if (item.getTypeId() == ItemID.MUSHROOM_SOUP)
            item.setTypeId(ItemID.BOWL); //Get your bowl back
        else if (item.getTypeId() == ItemID.POTION)
            item.setTypeId(ItemID.GLASS_BOTTLE); //Get your bottle back
        else if (item.getTypeId() == ItemID.LAVA_BUCKET || item.getTypeId() == ItemID.WATER_BUCKET ||
                item.getTypeId() == ItemID.MILK_BUCKET)
            item.setTypeId(ItemID.BUCKET); //Get your bucket back
        else if (item.getAmount() == 1)
            item.setTypeId(0);
        else
            item.setAmount(item.getAmount() - 1);
        return item;
    }
}