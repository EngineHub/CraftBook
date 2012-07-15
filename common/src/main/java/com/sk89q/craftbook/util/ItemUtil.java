package com.sk89q.craftbook.util;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

public class ItemUtil {

    public static boolean areItemsSimilar(ItemStack item, ItemStack item2) {
        if(item.getTypeId() == item2.getTypeId()) return true;
        return false;
    }

    public static boolean areItemsIdentical(ItemStack item, ItemStack item2) {
        if(item.getTypeId() == item2.getTypeId()) {
            if(item.getData() == item2.getData()) {
                return true;
            }
        }
        return false;
    }

    public static boolean isItemSimilarTo(ItemStack item, int type) {
        if(item.getTypeId() == type) return true;
        return false;
    }

    public static boolean isItemIdenticalTo(ItemStack item, int type, byte data) {
        if(item.getTypeId() == type) {
            if(item.getData().getData() == (byte) data) {
                return true;
            }
        }
        return false;
    }

    public static void setItemTypeAndData(ItemStack item, int type, byte data) {
        item.setTypeId(type);
        item.setData(new MaterialData(type,data));
    }

    public static boolean isStackValid(ItemStack item) {
        if(item == null) return false;
        if(item.getAmount() > 0 && item.getTypeId() > 0) return true;
        return false;
    }

    public static boolean isItemSmeltable(ItemStack item, Boolean foodOnly) {
        if(!foodOnly == false) {
            if(item.getType() == Material.RAW_BEEF) return true;
            if(item.getType() == Material.RAW_CHICKEN) return true;
            if(item.getType() == Material.RAW_FISH) return true;
            if(item.getType() == Material.PORK) return true;
        }
        if(!foodOnly == true) {
            if(item.getType() == Material.IRON_ORE) return true;
            if(item.getType() == Material.GOLD_ORE) return true;
            if(item.getType() == Material.DIAMOND_ORE) return true;
            if(item.getType() == Material.SAND) return true;
            if(item.getType() == Material.CLAY_BALL) return true;
        }
        return false;
    }

    public static boolean containsRawFood(Inventory inv) {
        for(ItemStack it : inv.getContents())
            if(it!=null && isItemSmeltable(it,true)) return true;
        return false;
    }

    public static boolean containsRawMinerals(Inventory inv) {
        for(ItemStack it : inv.getContents())
            if(it!=null && isItemSmeltable(it,false)) return true;
        return false;
    }

    public static ItemStack getSmeltedState(ItemStack item, Boolean foodOnly) { //foodOnly, null = anything, false = mineral, true = food
        if(isItemSmeltable(item, null)) {
            if(!foodOnly == false) {
                if(item.getType() == Material.RAW_BEEF) return new ItemStack(Material.COOKED_BEEF, 1);
                if(item.getType() == Material.RAW_CHICKEN) return new ItemStack(Material.COOKED_CHICKEN, 1);
                if(item.getType() == Material.RAW_FISH) return new ItemStack(Material.COOKED_FISH, 1);
                if(item.getType() == Material.PORK) return new ItemStack(Material.GRILLED_PORK, 1);
            }
            if(!foodOnly == true) {
                if(item.getType() == Material.IRON_ORE) return new ItemStack(Material.IRON_INGOT, 1);
                if(item.getType() == Material.GOLD_ORE) return new ItemStack(Material.GOLD_INGOT, 1);
                if(item.getType() == Material.DIAMOND_ORE) return new ItemStack(Material.DIAMOND, 1);
                if(item.getType() == Material.SAND) return new ItemStack(Material.GLASS, 1);
                if(item.getType() == Material.CLAY_BALL) return new ItemStack(Material.CLAY_BRICK, 1);
            }
        }
        return null;
    }

    public static boolean isItemEdible(ItemStack item) {
        return item.getType().isEdible();
    }
}
