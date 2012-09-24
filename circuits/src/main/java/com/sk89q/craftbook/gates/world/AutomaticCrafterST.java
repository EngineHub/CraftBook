package com.sk89q.craftbook.gates.world;

import org.bukkit.Server;
import org.bukkit.block.Sign;

import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.SelfTriggeredIC;

public class AutomaticCrafterST extends AutomaticCrafter implements SelfTriggeredIC{

    public AutomaticCrafterST(Server server, Sign block) {
        super(server, block);
    }

    @Override
    public String getTitle() {
        return "Automatic Crafter ST";
    }

    @Override
    public String getSignTitle() {
        return "AUTO CRAFT ST";
    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public void trigger(ChipState state) {
        state.setOutput(0, doStuff(true, true));
    }

    @Override
    public void think(ChipState state) {
        state.setOutput(0, doStuff(false, true));
    }

    public static class Factory extends AutomaticCrafter.Factory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(Sign sign) {

            return new AutomaticCrafterST(getServer(), sign);
        }
    }
}
