package com.sk89q.craftbook.gates.world;

import org.bukkit.Server;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.ICFactory;
import com.sk89q.craftbook.ic.RestrictedIC;
import com.sk89q.craftbook.ic.SelfTriggeredIC;

public class LiquidFloodST extends LiquidFlood implements SelfTriggeredIC {

    public LiquidFloodST(Server server, ChangedSign block, ICFactory factory) {

        super(server, block, factory);
    }

    @Override
    public String getTitle() {

        return "Self-Triggered Liquid Flooder";
    }

    @Override
    public String getSignTitle() {

        return "LIQUID FLOOD ST";
    }

    @Override
    public boolean isActive() {

        return true;
    }

    @Override
    public void think(ChipState state) {

        doStuff(state);
    }

    public static class Factory extends LiquidFlood.Factory implements
    RestrictedIC {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new LiquidFloodST(getServer(), sign, this);
        }
    }
}
