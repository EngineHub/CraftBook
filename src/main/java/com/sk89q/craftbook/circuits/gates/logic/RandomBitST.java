package com.sk89q.craftbook.circuits.gates.logic;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.circuits.ic.*;
import org.bukkit.Server;

public class RandomBitST extends RandomBit implements SelfTriggeredIC {

    public RandomBitST(Server server, ChangedSign block, ICFactory factory) {

        super(server, block, factory);
    }

    @Override
    public String getTitle() {

        return "Random Bit";
    }

    @Override
    public String getSignTitle() {

        return "RANDOM BIT";
    }

    @Override
    public void think(ChipState chip) {

        if (chip.getInput(0)) {
            for (short i = 0; i < chip.getOutputCount(); i++) {
                chip.setOutput(i, CraftBookPlugin.inst().getRandom().nextBoolean());
            }
        }
    }

    public static class Factory extends RandomBit.Factory implements RestrictedIC { // Restricted as could lag

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new RandomBitST(getServer(), sign, this);
        }
    }

    @Override
    public boolean isActive() {

        return true;
    }

    @Override
    public void trigger(ChipState chip) {

    }
}
