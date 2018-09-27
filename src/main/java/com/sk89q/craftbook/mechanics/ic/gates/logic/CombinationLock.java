package com.sk89q.craftbook.mechanics.ic.gates.logic;

import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Server;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.mechanics.ic.AbstractIC;
import com.sk89q.craftbook.mechanics.ic.AbstractICFactory;
import com.sk89q.craftbook.mechanics.ic.ChipState;
import com.sk89q.craftbook.mechanics.ic.IC;
import com.sk89q.craftbook.mechanics.ic.ICFactory;
import com.sk89q.craftbook.mechanics.ic.ICVerificationException;

/**
 * @author Me4502
 */
public class CombinationLock extends AbstractIC {

    public CombinationLock(Server server, ChangedSign block, ICFactory factory) {

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
                if (state.getInput(0) != (data[1] == 'X'))
                    break checkCombo;
                if (state.getInput(1) != (data[2] == 'X'))
                    break checkCombo;
                if (state.getInput(2) != (data[0] == 'X'))
                    break checkCombo;

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
        public IC create(ChangedSign sign) {

            return new CombinationLock(getServer(), sign, this);
        }

        @Override
        public void verify(ChangedSign sign) throws ICVerificationException {

            if (sign.getLine(2) == null && sign.getLine(2).isEmpty())
                throw new ICVerificationException("Line three needs to be a combination");
        }

        @Override
        public String getShortDescription() {

            return "Checks combination on sign against inputs.";
        }

        @Override
        public String[] getPinDescription(ChipState state) {

            return new String[] {
                    "Combination Bit 1",//Inputs
                    "Combination Bit 2",
                    "Combination Bit 3",
                    "High on Correct Combination"//Outputs
            };
        }

        @Override
        public String[] getLineHelp() {

            return new String[] {"Combination. X = On, O = Off (XOX)", null};
        }
    }
}
