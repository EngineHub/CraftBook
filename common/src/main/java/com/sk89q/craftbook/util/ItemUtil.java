package com.sk89q.craftbook.util;

import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.blocks.ItemID;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

public class ItemUtil {

    public static boolean areItemsSimilar(ItemStack item, ItemStack item2) {

        return item.getTypeId() == item2.getTypeId();
    }

    public static boolean areItemsIdentical(ItemStack item, ItemStack item2) {

        if (item.getTypeId() == item2.getTypeId()) {
            if (item.getData() == item2.getData()) {
                return true;
            }
        }
        return false;
    }

    public static boolean isItemSimilarTo(ItemStack item, int type) {

        return item.getTypeId() == type;
    }

    public static boolean isItemIdenticalTo(ItemStack item, int type, byte data) {

        if (item.getTypeId() == type) {
            if (item.getData().getData() == data) {
                return true;
            }
        }
        return false;
    }

    public static void setItemTypeAndData(ItemStack item, int type, byte data) {

        item.setTypeId(type);
        item.setData(new MaterialData(type, data));
    }

    public static boolean isStackValid(ItemStack item) {

        return item != null && item.getAmount() > 0 && item.getTypeId() > 0;
    }

    public static boolean isItemSmeltable(ItemStack item, Boolean foodOnly) {

        if (foodOnly == null || foodOnly) {
            if (item.getType() == Material.RAW_BEEF) return true;
            if (item.getType() == Material.RAW_CHICKEN) return true;
            if (item.getType() == Material.RAW_FISH) return true;
            if (item.getType() == Material.PORK) return true;
        }
        if (foodOnly == null || !foodOnly) {
            if (item.getType() == Material.IRON_ORE) return true;
            if (item.getType() == Material.GOLD_ORE) return true;
            if (item.getType() == Material.DIAMOND_ORE) return true;
            if (item.getType() == Material.SAND) return true;
            if (item.getType() == Material.CLAY_BALL) return true;
        }
        return false;
    }

    public static boolean containsRawFood(Inventory inv) {

        for (ItemStack it : inv.getContents())
            if (it != null && isItemSmeltable(it, true)) return true;
        return false;
    }

    public static boolean containsRawMinerals(Inventory inv) {

        for (ItemStack it : inv.getContents())
            if (it != null && isItemSmeltable(it, false)) return true;
        return false;
    }

    public static ItemStack getSmeltedState(ItemStack item, Boolean foodOnly) { //foodOnly, null = anything,
    // false = mineral, true = food
        if (isItemSmeltable(item, null)) {
            if (foodOnly == null || foodOnly) {
                switch (item.getTypeId()) {
                    case ItemID.RAW_BEEF:
                        return new ItemStack(ItemID.COOKED_BEEF);
                    case ItemID.RAW_CHICKEN:
                        return new ItemStack(ItemID.COOKED_CHICKEN);
                    case ItemID.RAW_FISH:
                        return new ItemStack(ItemID.COOKED_FISH);
                    case ItemID.RAW_PORKCHOP:
                        return new ItemStack(ItemID.COOKED_PORKCHOP);
                }
            }
            if (foodOnly == null || !foodOnly) {
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
                }
            }
        }
        return null;
    }

    public static boolean isItemEdible(ItemStack item) {

        return item.getType().isEdible();
    }
}
