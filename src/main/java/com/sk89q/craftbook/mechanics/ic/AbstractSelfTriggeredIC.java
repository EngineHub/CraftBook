package com.sk89q.craftbook.mechanics.ic;

import org.bukkit.Server;

import com.sk89q.craftbook.ChangedSign;

public abstract class AbstractSelfTriggeredIC extends AbstractIC implements SelfTriggeredIC {

    public AbstractSelfTriggeredIC (Server server, ChangedSign sign, ICFactory factory) {
        super(server, sign, factory);
    }

    @Override
    public void think(ChipState chip) {
        trigger(chip);
    }

    @Override
    public boolean isActive() {

        return true;
    }

    @Override
    public boolean isAlwaysST() {

        return false;
    }
}