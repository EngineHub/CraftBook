package com.sk89q.craftbook.mechanics.ic.gates.logic;

import org.bukkit.Server;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.mechanics.ic.AbstractIC;
import com.sk89q.craftbook.mechanics.ic.ChipState;
import com.sk89q.craftbook.mechanics.ic.ICFactory;

public abstract class SimpleTwoInputLogicGate extends AbstractIC {

    public SimpleTwoInputLogicGate(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    @Override
    public void trigger(ChipState chip) {

        Boolean a = null;
        Boolean b = null;

        // New input handling: any/first two valid inputs discovered. Moar flexibility!
        for (short i = 0; i < chip.getInputCount(); i++) {
            if (chip.isValid(i)) {
                boolean pinval = chip.getInput(i);
                // Got pin value, assign to first free variable, break if got both.
                if (a == null) {
                    a = pinval;
                } else if (b == null) {
                    b = pinval;
                } else {
                    break;
                }
            }
        }

        if (a == null || b == null) return;

        chip.setOutput(0, getResult(a, b));
    }

    protected abstract boolean getResult(boolean a, boolean b);
}
