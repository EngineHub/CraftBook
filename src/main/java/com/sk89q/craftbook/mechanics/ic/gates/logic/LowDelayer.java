package com.sk89q.craftbook.mechanics.ic.gates.logic;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.scheduler.BukkitTask;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.mechanics.ic.AbstractIC;
import com.sk89q.craftbook.mechanics.ic.AbstractICFactory;
import com.sk89q.craftbook.mechanics.ic.ChipState;
import com.sk89q.craftbook.mechanics.ic.IC;
import com.sk89q.craftbook.mechanics.ic.ICFactory;
import com.sk89q.craftbook.mechanics.ic.ICVerificationException;

/**
 * @author Silthus
 */
public class LowDelayer extends AbstractIC {

    private BukkitTask taskId;

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
            if(taskId != null)
                taskId.cancel();
            chip.setOutput(0, true);
        } else {
            taskId = Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), () -> {

                if (!chip.getInput(0)) {
                    chip.setOutput(0, false);
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
        public String[] getPinDescription(ChipState state) {

            return new String[] {
                    "Trigger IC",//Inputs
                    "Delayed Output",//Outputs
            };
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
