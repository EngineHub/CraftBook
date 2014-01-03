package com.sk89q.craftbook;

public abstract class AbstractCraftBookMechanic implements CraftBookMechanic {

    @Override
    public boolean enable() {
        return true;
    }

    @Override
    public void disable() {

    }
}