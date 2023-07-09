/*
 * CraftBook Copyright (C) EngineHub and Contributors <https://enginehub.org/>
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not,
 * see <http://www.gnu.org/licenses/>.
 */

package org.enginehub.craftbook.mechanics.ic.gates.logic;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.scheduler.BukkitTask;
import org.enginehub.craftbook.ChangedSign;
import org.enginehub.craftbook.bukkit.CraftBookPlugin;
import org.enginehub.craftbook.mechanics.ic.AbstractIC;
import org.enginehub.craftbook.mechanics.ic.AbstractICFactory;
import org.enginehub.craftbook.mechanics.ic.ChipState;
import org.enginehub.craftbook.mechanics.ic.IC;
import org.enginehub.craftbook.mechanics.ic.ICFactory;
import org.enginehub.craftbook.mechanics.ic.ICVerificationException;

/**
 * @author Silthus
 */
public class Delayer extends AbstractIC {

    private BukkitTask taskId;
    private long delay = 1;
    private boolean tickDelay;
    private boolean stayOnLow;

    public Delayer(Server server, ChangedSign block, ICFactory factory) {

        super(server, block, factory);
    }

    @Override
    public void load() {
        delay = Long.parseLong(getLine(2));
        tickDelay = Boolean.parseBoolean(getLine(3).split(":")[0]);
        if (getLine(3).contains(":"))
            stayOnLow = Boolean.parseBoolean(getLine(3).split(":")[1]);
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

        long tdelay = delay * 20;
        if (tickDelay) tdelay = delay;
        if (chip.getInput(0)) {
            taskId = Bukkit.getScheduler().runTaskLater(CraftBookPlugin.inst(), () -> chip.setOutput(0, true), tdelay);
        } else {
            if (taskId != null && !stayOnLow)
                taskId.cancel();
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
                Integer.parseInt(PlainTextComponentSerializer.plainText().serialize(sign.getLine(2)));
            } catch (Exception ignored) {
                throw new ICVerificationException("The third line needs to be a number.");
            }
        }

        @Override
        public String getShortDescription() {

            return "Delays signal by X seconds (or ticks if set).";
        }

        @Override
        public String[] getPinDescription(ChipState state) {

            return new String[] {
                "Trigger IC",//Inputs
                "Delayed Output",//Outputs
            };
        }

        @Override
        public String[] getLineHelp() {

            return new String[] { "seconds", "true to use ticks:true to continue on low" };
        }
    }
}
