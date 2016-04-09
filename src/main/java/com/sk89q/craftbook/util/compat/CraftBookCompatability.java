package com.sk89q.craftbook.util.compat;

import org.bukkit.entity.Player;

public interface CraftBookCompatability {

    abstract void enable(Player player);

    abstract void disable(Player player);
}