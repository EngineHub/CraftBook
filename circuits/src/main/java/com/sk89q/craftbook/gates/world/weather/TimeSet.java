package com.sk89q.craftbook.gates.world.weather;

import org.bukkit.Server;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.BukkitUtil;
import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.ICFactory;
import com.sk89q.craftbook.ic.RestrictedIC;

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

            String[] lines = new String[] {
                    "time to set",
                    null
            };
            return lines;
        }
    }
}