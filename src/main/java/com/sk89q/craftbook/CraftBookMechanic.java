package com.sk89q.craftbook;

import org.bukkit.event.Listener;

public interface CraftBookMechanic extends Listener {

    public boolean enable();

    public void disable();
}