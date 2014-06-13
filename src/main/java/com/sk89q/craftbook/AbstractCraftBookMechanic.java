package com.sk89q.craftbook;

import com.sk89q.craftbook.util.LoadPriority;

public abstract class AbstractCraftBookMechanic implements CraftBookMechanic, Comparable<LoadPriority> {

    @Override
    public boolean enable() {
        return true;
    }

    @Override
    public void disable() {

    }

    @Override
    public LoadPriority getLoadPriority() {

        return LoadPriority.STANDARD;
    }

    @Override
    public int compareTo(LoadPriority compare) {

        return compare.index < getLoadPriority().index ? -1 : 1;
    }
}