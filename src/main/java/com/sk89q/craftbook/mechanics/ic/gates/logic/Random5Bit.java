package com.sk89q.craftbook.mechanics.ic.gates.logic;

import org.bukkit.Server;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.mechanics.ic.IC;
import com.sk89q.craftbook.mechanics.ic.ICFactory;

public class Random5Bit extends RandomBit {

    public Random5Bit(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    @Override
    public String getTitle() {

        return "Random 5-Bit";
    }

    @Override
    public String getSignTitle() {

        return "5-BIT RANDOM";
    }

    public static class Factory extends RandomBit.Factory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public String[] getLongDescription() {

            return new String[]{
                    "The '''MC6020''' generates 5 random bits whenever the input (the \"clock\") goes from low to high."
            };
        }

        @Override
        public IC create(ChangedSign sign) {

            return new Random5Bit(getServer(), sign, this);
        }
    }

}