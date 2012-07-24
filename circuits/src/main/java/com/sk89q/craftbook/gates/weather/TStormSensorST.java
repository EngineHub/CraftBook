package com.sk89q.craftbook.gates.weather;


import com.sk89q.craftbook.ic.*;
import org.bukkit.Server;
import org.bukkit.block.Sign;

public class TStormSensorST extends AbstractIC implements SelfTriggeredIC {

    public TStormSensorST(Server server, Sign sign) {

        super(server, sign);
    }

    @Override
    public String getTitle() {

        return "Is It a Storm";
    }

    @Override
    public String getSignTitle() {

        return "IS IT A STORM";
    }

    @Override
    public void trigger(ChipState chip) {

    }

    @Override
    public void think(ChipState chip) {

        chip.setOutput(0, getSign().getWorld().isThundering());
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

            return new TStormSensorST(getServer(), sign);
        }
    }

}
