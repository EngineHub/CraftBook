package com.sk89q.craftbook.circuits.gates.logic;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.circuits.ic.*;
import org.bukkit.Bukkit;
import org.bukkit.Server;

/**
 * @author Silthus
 */
public class LowDelayer extends AbstractIC {

    private int taskId;

    public LowDelayer(Server server, ChangedSign block, ICFactory factory) {

        super(server, block, factory);
    }

    @Override
    public String getTitle() {

        return "Low Delayer";
    }

    @Override
    public String getSignTitle() {

        return "LOW_DELAYER";
    }

    @Override
    public void trigger(final ChipState chip) {

        long delay = Long.parseLong(getSign().getLine(2));
        if (chip.getInput(0)) {
            Bukkit.getScheduler().cancelTask(taskId);
            chip.setOutput(0, true);
        } else {
            taskId = Bukkit.getScheduler().scheduleSyncDelayedTask(CraftBookPlugin.inst(), new Runnable() {

                @Override
                public void run() {

                    if (!chip.getInput(0)) {
                        chip.setOutput(0, false);
                    }
                }
            }, delay * 20);
        }
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new LowDelayer(getServer(), sign, this);
        }

        @Override
        public void verify(ChangedSign sign) throws ICVerificationException {

            try {
                Integer.parseInt(sign.getLine(2));
            } catch (Exception ignored) {
                throw new ICVerificationException("The third line needs to be a number.");
            }
        }
    }
}
