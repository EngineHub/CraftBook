package com.sk89q.craftbook.util;

import org.bukkit.Material;
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
    
    public static boolean isItemCookable(ItemStack item) {
	if(item.getType() == Material.RAW_BEEF) return true;
	if(item.getType() == Material.RAW_CHICKEN) return true;
	if(item.getType() == Material.RAW_FISH) return true;
	if(item.getType() == Material.PORK) return true;
	return false;
    }
    
    public static ItemStack getCookedState(ItemStack item) {
	if(isItemCookable(item)) {
	    
	}
	return null;
    }
    
    public static boolean isItemEdible(ItemStack item) {
	return item.getType().isEdible();
    }
}
