package com.sk89q.craftbook.gates.world.miscellaneous;

import org.bukkit.Server;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.ICFactory;
import com.sk89q.craftbook.ic.SelfTriggeredIC;

public class PotionInducerST extends PotionInducer implements SelfTriggeredIC {

    public PotionInducerST (Server server, ChangedSign sign, ICFactory factory) {
        super(server, sign, factory);
    }

    @Override
    public String getTitle() {

        return "Self-Triggered Potion Inducer";
    }

    @Override
    public String getSignTitle() {

        return "POTION INDUCER ST";
    }

    @Override
    public void think(ChipState state) {

        state.setOutput(0, induce());
    }


    @Override
    public boolean isActive() {

        return true;
    }

    public static class Factory extends PotionInducer.Factory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new PotionInducerST(getServer(), sign, this);
        }
    }
}