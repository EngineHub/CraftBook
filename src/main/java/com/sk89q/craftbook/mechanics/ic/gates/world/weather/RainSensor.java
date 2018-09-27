package com.sk89q.craftbook.mechanics.ic.gates.world.weather;

import org.bukkit.Server;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.util.CraftBookBukkitUtil;
import com.sk89q.craftbook.mechanics.ic.AbstractICFactory;
import com.sk89q.craftbook.mechanics.ic.AbstractSelfTriggeredIC;
import com.sk89q.craftbook.mechanics.ic.ChipState;
import com.sk89q.craftbook.mechanics.ic.IC;
import com.sk89q.craftbook.mechanics.ic.ICFactory;

public class RainSensor extends AbstractSelfTriggeredIC {

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
            chip.setOutput(0, CraftBookBukkitUtil.toSign(getSign()).getWorld().hasStorm());
        }
    }

    @Override
    public void think(ChipState chip) {

        chip.setOutput(0, CraftBookBukkitUtil.toSign(getSign()).getWorld().hasStorm());
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
        public String[] getPinDescription(ChipState state) {

            return new String[] {
                    "Trigger IC",//Inputs
                    "High if raining"//Outputs
            };
        }

        @Override
        public String getShortDescription() {

            return "Outputs high if it is raining.";
        }
    }
}