package com.sk89q.craftbook.gates.world.weather;

import java.util.regex.Pattern;

import org.bukkit.Server;
import org.bukkit.World;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.BukkitUtil;
import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.ICFactory;

public class WeatherControl extends AbstractIC {

    private static final Pattern RIGHT_BRACKET_PATTERN = Pattern.compile("]", Pattern.LITERAL);

    public WeatherControl (Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    @Override
    public String getTitle () {

        return "Weather Control";
    }

    @Override
    public String getSignTitle () {

        return "WEATHER CONTROL";
    }

    @Override
    public void trigger (ChipState chip) {

        boolean tstorm = false;
        int duration = 24000;
        int thunderDuration = duration;
        try {
            String[] st = RIGHT_BRACKET_PATTERN.split(getSign().getLine(1), 2);
            if (st.length > 1) {
                tstorm = st[1].equalsIgnoreCase("t");
            }
            duration = Integer.parseInt(getSign().getLine(2));
        } catch (Exception ignored) {
        }
        try {
            thunderDuration = Integer.parseInt(getSign().getLine(3));
        } catch (Exception ignored) {
        }

        if (duration > 24000) {
            duration = 24000;
        }
        if (duration < 1) {
            duration = 1;
        }

        if (thunderDuration > 24000) {
            thunderDuration = 24000;
        }
        if (thunderDuration < 1) {
            thunderDuration = 1;
        }

        World world = BukkitUtil.toSign(getSign()).getWorld();
        if (chip.getInput(0)) {
            world.setStorm(true);
            world.setWeatherDuration(duration);
            if (tstorm) {
                world.setThundering(true);
                world.setThunderDuration(thunderDuration);
            }
            chip.setOutput(0, true);
        } else {
            world.setThundering(false);
            world.setStorm(false);
            chip.setOutput(0, false);
        }
    }

    public static class Factory extends AbstractICFactory {

        public Factory (Server server) {

            super(server);
        }

        @Override
        public IC create (ChangedSign sign) {

            return new WeatherControl(getServer(), sign, this);
        }

        @Override
        public String getDescription () {

            return "Set rain and thunder duration.";
        }

        @Override
        public String[] getLineHelp () {

            String[] lines = new String[] { "rain duration", "thunder duration" };
            return lines;
        }
    }
}