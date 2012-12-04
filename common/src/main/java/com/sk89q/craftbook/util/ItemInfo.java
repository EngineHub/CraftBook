package com.sk89q.craftbook.util;

public class ItemInfo {

    public int id;
    public byte data;

    public ItemInfo(int id, byte data) {

        this.id = id;
        this.data = data;
    }

    public int getId () {
        return id;
    }

    public void setId (int id) {
        this.id = id;
    }

    public byte getData () {
        return data;
    }

    public void setData (byte data) {
        this.data = data;
    }

    public static ItemInfo parseFromString(String string) {

        return new ItemInfo(Integer.parseInt(string.split(":")[0]),Byte.parseByte(string.split(":")[1]));
    }
}