package com.sk89q.craftbook.util;

public enum LoadPriority {

    EARLY(0),
    STANDARD(1),
    LATE(2);

    public int index;

    LoadPriority(int index) {

        this.index = index;
    }
}