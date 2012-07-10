package com.sk89q.craftbook.gates.world;

import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Server;
import org.bukkit.block.Sign;

import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.SelfTriggeredIC;

public class CombinationLock extends AbstractIC implements SelfTriggeredIC{

    public CombinationLock(Server server, Sign block) {
        super(server, block);
    }

    @Override
    public String getTitle() {
        return "Combination Lock";
    }

    @Override
    public String getSignTitle() {
        return "COMBINATION LOCK";
    }

    @Override
    public void trigger(ChipState chip) {

    }

    public static class Factory extends AbstractICFactory {
        public Factory(Server server) {
            super(server);
        }

        @Override
        public IC create(Sign sign) {
            return new CombinationLock(getServer(), sign);
        }
    }

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public void think(ChipState state) {
        try {
            Character[] data = ArrayUtils.toObject(getSign().getLine(2).toCharArray());
            checkCombo: {
                if(!state.get(0) == data[0].equals('X')) break checkCombo;
                if(!state.get(1) == data[1].equals('X')) break checkCombo;
                if(!state.get(2) == data[2].equals('X')) break checkCombo;

                state.setOutput(0, true);
                return;
            }
            state.setOutput(0, false);
        }
        catch(Exception e) {
            state.setOutput(0, false);
        }
    }
}
