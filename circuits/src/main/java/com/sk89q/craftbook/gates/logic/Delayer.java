package com.sk89q.craftbook.gates.logic;

import org.bukkit.Bukkit;
import org.bukkit.Server;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.CircuitsPlugin;
import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.ICFactory;
import com.sk89q.craftbook.ic.ICVerificationException;

/**
 * @author Silthus
 */
public class Delayer extends AbstractIC {

    private long delay = 1;

    public Delayer(Server server, ChangedSign block, ICFactory factory) {

        super(server, block, factory);
        try {
            delay = Long.parseLong(getSign().getLine(2));
        } catch (Exception ignored) {
        }
    }

    @Override
    public String getTitle() {

        return "Delayer";
    }

    @Override
    public String getSignTitle() {

        return "DELAYER";
    }

    @Override
    public void trigger(final ChipState chip) {

        if (chip.getInput(0)) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(CircuitsPlugin.getInst(), new Runnable() {

                @Override
                public void run() {

                    if (chip.getInput(0)) {
                        chip.setOutput(0, true);
                    }
                }
            }, delay * 20);
        } else {
            chip.setOutput(0, false);
        }
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new Delayer(getServer(), sign, this);
        }

        @Override
        public void verify(ChangedSign sign) throws ICVerificationException {

            try {
                Integer.parseInt(sign.getLine(2));
            } catch (Exception ignored) {
                throw new ICVerificationException("The third line needs to be a number.");
            }
        }

        @Override
        public String getDescription() {

            return "Delays signal by X seconds.";
        }

        @Override
        public String[] getLineHelp() {

            String[] lines = new String[] {
                    "seconds",
                    null
            };
            return lines;
        }
    }
}
