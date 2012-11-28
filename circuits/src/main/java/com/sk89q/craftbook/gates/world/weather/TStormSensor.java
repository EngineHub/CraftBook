package com.sk89q.craftbook.gates.world.weather;


import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.BukkitUtil;
import com.sk89q.craftbook.ic.*;
import org.bukkit.Server;

public class TStormSensor extends AbstractIC {

    public TStormSensor(Server server, ChangedSign sign, ICFactory factory) {

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

        if (chip.getInput(0)) {
            chip.setOutput(0, BukkitUtil.toSign(getSign()).getWorld().isThundering());
        }
    }


    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new TStormSensor(getServer(), sign, this);
        }

        @Override
        public String getDescription() {

            return "Outputs high if it is storming.";
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