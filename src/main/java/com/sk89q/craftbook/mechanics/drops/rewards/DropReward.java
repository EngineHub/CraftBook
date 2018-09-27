package com.sk89q.craftbook.mechanics.drops.rewards;

import org.bukkit.entity.Player;

public abstract class DropReward {

    public String name;

    public DropReward(String name) {

        this.name = name;
    }

    public String getName() {

        return name;
    }

    public abstract void giveReward(Player player);

    public abstract boolean doesRequirePlayer();
}