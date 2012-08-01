package com.sk89q.craftbook.gates.world;

import com.sk89q.craftbook.ic.*;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Server;
import org.bukkit.block.Sign;

/**
 * @author Me4502
 */
public class CombinationLock extends AbstractIC implements SelfTriggeredIC {

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

    @Override
    public boolean isActive() {

        return false;
    }

    @Override
    public void think(ChipState state) {

        try {
            Character[] data = ArrayUtils.toObject(getSign().getLine(2).toCharArray());
            checkCombo:
            {
                for (short s = 0; s < state.getInputCount(); s++) {
                    if (!state.get(s) == data[s].equals('X')) break checkCombo;
                }

                state.setOutput(0, true);
                return;
            }
            state.setOutput(0, false);
        } catch (Exception e) {
            state.setOutput(0, false);
        }
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(Sign sign) {

            return new CombinationLock(getServer(), sign);
        }

        @Override
        public void verify(Sign sign) throws ICVerificationException {

            if (sign.getLine(2) == null && sign.getLine(2).equals("")) {
                throw new ICVerificationException("Line three needs to be a combination");
            }
        }
    }
}
