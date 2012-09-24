package com.sk89q.craftbook.gates.weather;


import com.sk89q.craftbook.ic.*;
import org.bukkit.Server;
import org.bukkit.block.Sign;

public class RainSensorST extends AbstractIC implements SelfTriggeredIC {

    public RainSensorST(Server server, Sign sign) {

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

    }

    @Override
    public void think(ChipState chip) {

        chip.setOutput(0, getSign().getWorld().hasStorm());
    }

    @Override
    public boolean isActive() {

        return true;
    }


    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(Sign sign) {

            return new RainSensorST(getServer(), sign);
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