package com.sk89q.craftbook.mechanics.ic.gates.logic;

import com.github.Anon8281.universalScheduler.scheduling.tasks.MyScheduledTask;
import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.mechanics.ic.*;
import org.bukkit.Server;

/**
 * @author Silthus
 */
public class NotLowDelayer extends AbstractIC {

    private MyScheduledTask taskId;

    public NotLowDelayer(Server server, ChangedSign block, ICFactory factory) {

        super(server, block, factory);
    }

    @Override
    public String getTitle() {

        return "Not Low Delayer";
    }

    @Override
    public String getSignTitle() {

        return "NOT_LOW_DELAYER";
    }

    @Override
    public void trigger(final ChipState chip) {

        long delay = Long.parseLong(getSign().getLine(2));
        if (chip.getInput(0)) {
            if(taskId != null)
                taskId.cancel();
            chip.setOutput(0, false);
        } else {
            taskId = CraftBookPlugin.getScheduler().runTaskLater(() -> {

                if (!chip.getInput(0)) {
                    chip.setOutput(0, true);
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

            return new NotLowDelayer(getServer(), sign, this);
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
