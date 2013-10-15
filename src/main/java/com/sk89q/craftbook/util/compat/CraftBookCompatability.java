package com.sk89q.craftbook.util.compat;

import org.bukkit.entity.Player;

public abstract class CraftBookCompatability {

    public abstract void enable(Player player);

    public abstract void disable(Player player);
}