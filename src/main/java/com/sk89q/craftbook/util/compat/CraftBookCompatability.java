package com.sk89q.craftbook.util.compat;

import org.bukkit.entity.Player;

public interface CraftBookCompatability {

    void enable(Player player);

    void disable(Player player);
}