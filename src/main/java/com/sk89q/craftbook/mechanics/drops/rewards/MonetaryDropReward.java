package com.sk89q.craftbook.mechanics.drops.rewards;

import org.bukkit.entity.Player;

import com.sk89q.craftbook.bukkit.CraftBookPlugin;

public class MonetaryDropReward extends DropReward {

    public double amount;

    public MonetaryDropReward(String name, double amount) {

        super(name);
        this.amount = amount;
    }

    public double getAmount() {

        return amount;
    }

    @Override
    public void giveReward (Player player) {

        CraftBookPlugin.plugins.getEconomy().depositPlayer(player.getName(), amount);
    }

    @Override
    public boolean doesRequirePlayer () {
        return true;
    }
}