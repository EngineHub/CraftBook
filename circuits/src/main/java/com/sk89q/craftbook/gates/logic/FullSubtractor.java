package com.sk89q.craftbook.gates.logic;

import org.bukkit.Server;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.ICFactory;

public class FullSubtractor extends AbstractIC {

    public FullSubtractor(Server server, ChangedSign block, ICFactory factory) {

        super(server, block, factory);
    }

    @Override
    public String getTitle() {

        return "Full Subtractor";
    }

    @Override
    public String getSignTitle() {

        return "FULL SUBTRACTOR";
    }

    @Override
    public void trigger(ChipState chip) {

        boolean A = chip.getInput(0);
        boolean B = chip.getInput(1);
        boolean C = chip.getInput(2);

        boolean S = A ^ B ^ C;
        boolean Bo = C & !(A ^ B) | !A & B;

        chip.setOutput(0, S);
        chip.setOutput(1, Bo);
        chip.setOutput(2, Bo);
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new FullSubtractor(getServer(), sign, this);
        }
    }
}