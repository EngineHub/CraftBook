package com.sk89q.craftbook.util;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public class ItemInfo {

    public Material id;
    public int data;

    public ItemInfo(Material id, int data) {

        this.id = id;
        this.data = data;
    }

    @Deprecated
    public ItemInfo(int id, int data) {

        this.id = Material.getMaterial(id);
        this.data = data;
    }

    public ItemInfo(Block block) {

        id = block.getType();
        data = block.getData();
    }

    public ItemInfo(String string) {

        ItemStack stack = ItemSyntax.getItem(string);
        id = stack.getType();
        data = stack.getData().getData();
    }

    public int getId() {

        return id.getId();
    }

    public Material getType() {

        return id;
    }

    public void setId(int id) {

        this.id = Material.getMaterial(id);
    }

    public int getData() {

        return data;
    }

    public void setData(int data) {

        this.data = data;
    }

    public boolean isSame(Block block) {

        if(block.getType() == id)
            if(data == -1 || block.getData() == data)
                return true;
        return false;
    }

    public boolean isSame(ItemStack stack) {

        if(stack.getType() == id)
            if(data == -1 || stack.getData().getData() == data)
                return true;
        return false;
    }

    public static ItemInfo parseFromString(String string) {

        Material id = Material.getMaterial(RegexUtil.COLON_PATTERN.split(string)[0]);
        if(id == null)
            id = Material.getMaterial(Integer.parseInt(RegexUtil.COLON_PATTERN.split(string)[0]));
        int data = -1;

        try {
            data = Integer.parseInt(RegexUtil.COLON_PATTERN.split(string)[1]);
        } catch (Exception e) {
            data = -1;
        }

        return new ItemInfo(id, data);
    }

    public static List<ItemInfo> parseListFromString(List<String> strings) {

        List<ItemInfo> infos = new ArrayList<ItemInfo>();

        for(String string: strings)
            infos.add(parseFromString(string));

        return infos;
    }

    @Override
    public String toString() {

        return id + ":" + data;
    }

    @Override
    public int hashCode() {

        return (id.hashCode() * 1103515245 + 12345 ^ (data == -1 ? 0 : data) * 1103515245 + 12345) * 1103515245 + 12345;
    }

    @Override
    public boolean equals(Object object) {

        if (object instanceof ItemInfo) {

            ItemInfo it = (ItemInfo) object;
            if (it.getId() == getId()) {
                if (it.getData() == getData() || it.getData() == -1 || getData() == -1) return true;
            }
        }
        return false;
    }
}