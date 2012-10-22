package com.sk89q.craftbook.gates.world;

import com.sk89q.craftbook.ic.*;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Server;
import org.bukkit.block.Sign;

/**
 * @author Me4502
 */
public class CombinationLock extends AbstractIC {

    public CombinationLock(Server server, Sign block, ICFactory factory) {

        super(server, block, factory);
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
    public void trigger(ChipState state) {

        try {
            Character[] data = ArrayUtils.toObject(getSign().getLine(2).toCharArray());
            checkCombo:
            {
                if (state.getInput(0) != (data[1] == 'X')) {
                    break checkCombo;
                }
                if (state.getInput(1) != (data[2] == 'X')) {
                    break checkCombo;
                }
                if (state.getInput(2) != (data[0] == 'X')) {
                    break checkCombo;
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

            return new CombinationLock(getServer(), sign, this);
        }

        @Override
        public void verify(Sign sign) throws ICVerificationException {

            if (sign.getLine(2) == null && sign.getLine(2).equals(""))
                throw new ICVerificationException("Line three needs to be a combination");
        }

        @Override
        public String getDescription() {

            return "Checks combination on sign against inputs.";
        }

        @Override
        public String[] getLineHelp() {

            String[] lines = new String[] {
                    "Combination. X = On, O = Off (XOX)",
                    null
            };
            return lines;
        }
    }
}
