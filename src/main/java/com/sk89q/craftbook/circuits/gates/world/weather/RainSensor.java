package com.sk89q.craftbook.circuits.gates.world.weather;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.util.BukkitUtil;
import com.sk89q.craftbook.circuits.ic.*;
import org.bukkit.Server;

public class RainSensor extends AbstractIC {

    public RainSensor(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    @Override
    public String getTitle() {

        return "Is It Rain";
    }

    @Override
    public String getSignTitle() {

        return "IS IT RAIN";
    }

    @Override
    public void trigger(ChipState chip) {

        if (chip.getInput(0)) {
            chip.setOutput(0, BukkitUtil.toSign(getSign()).getWorld().hasStorm());
        }
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new RainSensor(getServer(), sign, this);
        }

        @Override
        public String getDescription() {

            return "Outputs high if it is raining.";
        }

        @Override
        public String[] getLineHelp() {

            String[] lines = new String[] {null, null};
            return lines;
        }
    }
}