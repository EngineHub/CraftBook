package com.sk89q.craftbook.gates.world;

import org.bukkit.Server;
import org.bukkit.block.Sign;

import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.RestrictedIC;

public class TimeSet extends AbstractIC {

    public TimeSet(Server server, Sign sign) {

        super(server, sign);
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
            if(chip.getInput(0))
                getSign().getWorld().setTime(Long.parseLong(getSign().getLine(2)));
        }
        catch(Exception e){}
    }

    public static class Factory extends AbstractICFactory implements RestrictedIC {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(Sign sign) {

            return new TimeSet(getServer(), sign);
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