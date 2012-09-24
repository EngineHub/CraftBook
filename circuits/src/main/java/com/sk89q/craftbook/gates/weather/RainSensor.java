package com.sk89q.craftbook.gates.weather;


import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import org.bukkit.Server;
import org.bukkit.block.Sign;

public class RainSensor extends AbstractIC {

    public RainSensor(Server server, Sign sign) {

        super(server, sign);
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

        if (chip.getInput(0)) chip.setOutput(0, getSign().getWorld().hasStorm());
    }


    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(Sign sign) {

            return new RainSensor(getServer(), sign);
        }

        @Override
        public String getDescription() {
            return "Outputs high if it is raining.";
        }

        @Override
        public String[] getLineHelp() {
            String[] lines = new String[] {
                    null,
                    null
            };
            return lines;
        }
    }
}