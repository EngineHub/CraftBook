package com.sk89q.craftbook.util;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

@SuppressWarnings("deprecation")
public class ItemInfo {

    public MaterialData data;

    public ItemInfo(Material id, int data) {

        this.data = new MaterialData(id, (byte) data);
    }

    @Deprecated
    public ItemInfo(int id, int data) {

        this.data = new MaterialData(id, (byte) data);
    }

    public ItemInfo(Block block) {

        data = new MaterialData(block.getType(), block.getData());
    }

    public ItemInfo(String string) {

        ItemStack stack = ItemSyntax.getItem(string);
        data = stack.getData();
    }

    @Deprecated
    public int getId() {

        return data.getItemTypeId();
    }

    public Material getType() {

        return data.getItemType();
    }

    public void setId(int id) {

        data = new MaterialData(id, data.getData());
    }

    public int getData() {

        return data.getData();
    }

    public void setData(int data) {

        this.data.setData((byte) data);
    }

    public MaterialData getMaterialData() {

        return data;
    }

    public boolean isSame(Block block) {

        if(block.getType() == data.getItemType())
            if(data.getData() == -1 || block.getData() == data.getData())
                return true;
        return false;
    }

    public boolean isSame(ItemStack stack) {

        if(stack.getType() == data.getItemType())
            if(data.getData() == -1 || stack.getData().getData() == data.getData())
                return true;
        return false;
    }

    public static List<ItemInfo> parseListFromString(List<String> strings) {

        List<ItemInfo> infos = new ArrayList<ItemInfo>();

        for(String string: strings)
            infos.add(new ItemInfo(string));

        return infos;
    }

    @Override
    public String toString() {

        return data.getItemType().name() + ":" + data.getData();
    }

    @Override
    public int hashCode() {

        return (data.getItemType().hashCode() * 1103515245 + 12345 ^ (data.getData() == -1 ? 0 : data.getData()) * 1103515245 + 12345) * 1103515245 + 12345;
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