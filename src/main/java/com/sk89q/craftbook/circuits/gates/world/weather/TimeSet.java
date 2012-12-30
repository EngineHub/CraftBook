package com.sk89q.craftbook.circuits.gates.world.weather;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.circuits.ic.*;
import org.bukkit.Server;

public class TimeSet extends AbstractIC {

    public TimeSet(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    @Override
    public String getTitle() {

        return "Time Set";
    }

    @Override
    public String getSignTitle() {

        return "TIME SET";
    }

    @Override
    public void load() {

        time = Long.parseLong(getSign().getLine(2));
    }

    /* it's been a */ long time;

    @Override
    public void trigger(ChipState chip) {

        try {
            if (chip.getInput(0)) {
                BukkitUtil.toSign(getSign()).getWorld().setTime(time);
            }
        } catch (Exception ignored) {
        }
    }

    public static class Factory extends AbstractICFactory implements RestrictedIC {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new TimeSet(getServer(), sign, this);
        }

        @Override
        public String getDescription() {

            return "Set time when triggered.";
        }

        @Override
        public String[] getLineHelp() {

            String[] lines = new String[] {"time to set", null};
            return lines;
        }
    }
}