package com.sk89q.craftbook.gates.logic;

import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.ICFactory;
import org.bukkit.Server;
import org.bukkit.block.Sign;

public class Random5Bit extends RandomBit {

    public Random5Bit(Server server, Sign sign, ICFactory factory) {

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


    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(Sign sign) {

            return new Random5Bit(getServer(), sign, this);
        }
    }

}