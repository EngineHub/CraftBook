package com.sk89q.craftbook.gates.world;

import com.sk89q.craftbook.ic.*;
import org.bukkit.Server;
import org.bukkit.block.Sign;

public class TimeSet extends AbstractIC {

    public TimeSet(Server server, Sign sign, ICFactory factory) {

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
    public void trigger(ChipState chip) {

        try {
            if (chip.getInput(0)) {
                getSign().getWorld().setTime(Long.parseLong(getSign().getLine(2)));
            }
        } catch (Exception ignored) {
        }
    }

    public static class Factory extends AbstractICFactory implements RestrictedIC {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(Sign sign) {

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