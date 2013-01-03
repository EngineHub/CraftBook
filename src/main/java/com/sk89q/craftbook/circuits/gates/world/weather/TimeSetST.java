package com.sk89q.craftbook.circuits.gates.world.weather;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.circuits.ic.*;
import org.bukkit.Server;

public class TimeSetST extends TimeSet implements SelfTriggeredIC {

    public TimeSetST(Server server, ChangedSign sign, ICFactory factory) {

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
                BukkitUtil.toSign(getSign()).getWorld().setTime(Long.parseLong(getSign().getLine(2)));
            }
        } catch (Exception ignored) {
        }
    }

    public static class Factory extends TimeSet.Factory implements RestrictedIC {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new TimeSetST(getServer(), sign, this);
        }

        @Override
        public String getShortDescription() {

            return "Sets time continuously.";
        }
    }
}