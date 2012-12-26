package com.sk89q.craftbook.util;

import com.sk89q.craftbook.RegexUtil;

public class ItemInfo {

    public int id;
    public int data;

    public ItemInfo(int id, int data) {

        this.id = id;
        this.data = data;
    }

    public int getId() {

        return id;
    }

    public void setId(int id) {

        this.id = id;
    }

    public int getData() {

        return data;
    }

    public void setData(int data) {

        this.data = data;
    }

    public static ItemInfo parseFromString(String string) {

        int id = Integer.parseInt(RegexUtil.COLON_PATTERN.split(string)[0]);
        int data = -1;

        try {
            data = Integer.parseInt(RegexUtil.COLON_PATTERN.split(string)[0]);
        } catch (Exception e) {
            data = -1;
        }

        return new ItemInfo(id, data);
    }

    @Override
    public String toString() {

        return id + ":" + data;
    }

    @Override
    public int hashCode() {

        return (id * 1103515245 + 12345 ^ (data == -1 ? 0 : data) * 1103515245 + 12345) * 1103515245 + 12345;
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
