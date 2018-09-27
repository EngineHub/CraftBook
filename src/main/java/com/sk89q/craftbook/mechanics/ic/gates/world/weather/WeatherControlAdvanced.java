package com.sk89q.craftbook.mechanics.ic.gates.world.weather;

import com.sk89q.craftbook.bukkit.util.CraftBookBukkitUtil;
import org.bukkit.Server;
import org.bukkit.World;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.mechanics.ic.AbstractIC;
import com.sk89q.craftbook.mechanics.ic.AbstractICFactory;
import com.sk89q.craftbook.mechanics.ic.ChipState;
import com.sk89q.craftbook.mechanics.ic.IC;
import com.sk89q.craftbook.mechanics.ic.ICFactory;
import com.sk89q.craftbook.mechanics.ic.RestrictedIC;

public class WeatherControlAdvanced extends AbstractIC {

    public WeatherControlAdvanced(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    @Override
    public String getTitle() {

        return "Weather Control";
    }

    @Override
    public String getSignTitle() {

        return "WEATHER CONTROL";
    }

    @Override
    public void trigger(ChipState chip) {

        int duration = 24000;
        int thunderDuration = duration;

        try {
            duration = Integer.parseInt(getSign().getLine(2));
            thunderDuration = Integer.parseInt(getSign().getLine(3));
        } catch (Exception ignored) {
        }

        if (duration > 24000) {
            duration = 24000;
        } else if (duration < 1) {
            duration = 1;
        }

        if (thunderDuration > 24000) {
            thunderDuration = 24000;
        } else if (thunderDuration < 1) {
            thunderDuration = 1;
        }

        if (chip.isTriggered(0) && chip.getInput(0)) {

            World world = CraftBookBukkitUtil.toSign(getSign()).getWorld();
            world.setStorm(chip.getInput(1));
            if (chip.getInput(1)) {
                world.setWeatherDuration(duration);
            }
            world.setThundering(chip.getInput(2));
            if (chip.getInput(2)) {
                world.setThunderDuration(thunderDuration);
            }
        }
    }

    public static class Factory extends AbstractICFactory implements RestrictedIC {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new WeatherControlAdvanced(getServer(), sign, this);
        }

        @Override
        public String getShortDescription() {

            return "When centre on, set rain if left high and thunder if right high.";
        }

        @Override
        public String[] getPinDescription(ChipState state) {

            return new String[] {
                    "Trigger IC",//Inputs
                    "High to rain",
                    "High to thunder",
                    "High on success"//Outputs
            };
        }

        @Override
        public String[] getLineHelp() {

            return new String[] {"rain duration", "thunder duration"};
        }
    }
}