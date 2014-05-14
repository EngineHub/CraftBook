package com.sk89q.craftbook.mechanics.ic.gates.logic;

import org.bukkit.Server;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.mechanics.ic.AbstractIC;
import com.sk89q.craftbook.mechanics.ic.ChipState;
import com.sk89q.craftbook.mechanics.ic.ICFactory;

public abstract class SimpleAnyInputLogicGate extends AbstractIC {

    public SimpleAnyInputLogicGate(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    @Override
    public void trigger(ChipState chip) {

        short on = 0, valid = 0;
        for (short i = 0; i < chip.getInputCount(); i++) {
            if (chip.isValid(i)) {
                valid++;

                if (chip.getInput(i)) {
                    on++;
                }
            }
        }

        // Condition; all valid must be ON, at least one valid.
        chip.setOutput(0, getResult(valid, on));
    }

    protected abstract boolean getResult(int wires, int on);
}
