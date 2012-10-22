package com.sk89q.craftbook.gates.world;

import org.bukkit.Server;
import org.bukkit.block.Sign;

import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.ICFactory;
import com.sk89q.craftbook.ic.RestrictedIC;
import com.sk89q.craftbook.ic.SelfTriggeredIC;

public class TimeSetST extends TimeSet implements SelfTriggeredIC {

    public TimeSetST(Server server, Sign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    @Override
    public boolean isActive() {

        return true;
    }

    @Override
    public String getTitle() {

        return "Time Set ST";
    }

    @Override
    public String getSignTitle() {

        return "TIME SET ST";
    }

    @Override
    public void think(ChipState chip) {

        try {
            if (chip.getInput(0)) {
                getSign().getWorld().setTime(Long.parseLong(getSign().getLine(2)));
            }
        } catch (Exception ignored) {
        }
    }

    public static class Factory extends TimeSet.Factory implements RestrictedIC {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(Sign sign) {

            return new TimeSetST(getServer(), sign, this);
        }

        @Override
        public String getDescription() {

            return "Sets time continuously.";
        }
    }
}