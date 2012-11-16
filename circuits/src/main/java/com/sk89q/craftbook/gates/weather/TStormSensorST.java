package com.sk89q.craftbook.gates.weather;


import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.BukkitUtil;
import com.sk89q.craftbook.ic.*;
import org.bukkit.Server;

public class TStormSensorST extends AbstractIC implements SelfTriggeredIC {

    public TStormSensorST(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
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

        chip.setOutput(0, BukkitUtil.toSign(getSign()).getWorld().isThundering());
    }

    @Override
    public boolean isActive() {

        return true;
    }

    public static class Factory extends TStormSensor.Factory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new TStormSensorST(getServer(), sign, this);
        }
    }
}