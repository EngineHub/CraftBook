package com.sk89q.craftbook.gates.world;

import com.sk89q.craftbook.ic.*;
import org.bukkit.Server;
import org.bukkit.block.Sign;

public class LiquidFloodST extends LiquidFlood implements SelfTriggeredIC {

    public LiquidFloodST(Server server, Sign block, ICFactory factory) {

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

    public static class Factory extends AbstractICFactory implements
    RestrictedIC {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(Sign sign) {

            return new LiquidFloodST(getServer(), sign, this);
        }

        @Override
        public String getDescription() {

            return "Floods an area with a liquid.";
        }

        @Override
        public String[] getLineHelp() {

            String[] lines = new String[] {
                    "water/lava",
                    "radius"
            };
            return lines;
        }
    }
}
