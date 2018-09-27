package com.sk89q.craftbook.mechanics.ic.gates.world.weather;

import com.sk89q.craftbook.bukkit.util.CraftBookBukkitUtil;
import org.bukkit.Server;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.mechanics.ic.AbstractICFactory;
import com.sk89q.craftbook.mechanics.ic.AbstractSelfTriggeredIC;
import com.sk89q.craftbook.mechanics.ic.ChipState;
import com.sk89q.craftbook.mechanics.ic.IC;
import com.sk89q.craftbook.mechanics.ic.ICFactory;

public class TStormSensor extends AbstractSelfTriggeredIC {

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
            chip.setOutput(0, CraftBookBukkitUtil.toSign(getSign()).getWorld().isThundering());
        }
    }

    @Override
    public void think(ChipState chip) {

        chip.setOutput(0, CraftBookBukkitUtil.toSign(getSign()).getWorld().isThundering());
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
        public String[] getPinDescription(ChipState state) {

            return new String[] {
                    "Trigger IC",//Inputs
                    "High if storming"//Outputs
            };
        }

        @Override
        public String getShortDescription() {

            return "Outputs high if it is storming.";
        }
    }
}