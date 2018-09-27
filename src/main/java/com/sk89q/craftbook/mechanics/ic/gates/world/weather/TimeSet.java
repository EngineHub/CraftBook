package com.sk89q.craftbook.mechanics.ic.gates.world.weather;

import org.bukkit.Server;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.util.CraftBookBukkitUtil;
import com.sk89q.craftbook.mechanics.ic.AbstractICFactory;
import com.sk89q.craftbook.mechanics.ic.AbstractSelfTriggeredIC;
import com.sk89q.craftbook.mechanics.ic.ChipState;
import com.sk89q.craftbook.mechanics.ic.IC;
import com.sk89q.craftbook.mechanics.ic.ICFactory;
import com.sk89q.craftbook.mechanics.ic.RestrictedIC;

public class TimeSet extends AbstractSelfTriggeredIC {

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

        try {
            time = Long.parseLong(getSign().getLine(2));
        } catch (NumberFormatException ex) {
            time = -1;
        }
    }

    /* it's been a */ long time;

    @Override
    public void trigger(ChipState chip) {

        try {
            if (chip.getInput(0) && time >= 0) {
                CraftBookBukkitUtil.toSign(getSign()).getWorld().setTime(time);
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void think(ChipState chip) {

        try {
            if (chip.getInput(0) && time >= 0) {
                CraftBookBukkitUtil.toSign(getSign()).getWorld().setTime(time);
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
        public String getShortDescription() {

            return "Set time when triggered.";
        }

        @Override
        public String[] getLineHelp() {

            return new String[] {"time to set", null};
        }
    }

    @Override
    public boolean isActive() {
        return true;
    }
}