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

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.enginehub.craftbook.ChangedSign;
import org.enginehub.craftbook.bukkit.CraftBookPlugin;
import org.enginehub.craftbook.mechanics.ic.AbstractIC;
import org.enginehub.craftbook.mechanics.ic.AbstractICFactory;
import org.enginehub.craftbook.mechanics.ic.ChipState;
import org.enginehub.craftbook.mechanics.ic.IC;
import org.enginehub.craftbook.mechanics.ic.ICFactory;
import org.enginehub.craftbook.mechanics.ic.ICVerificationException;
import org.enginehub.craftbook.mechanics.ic.RestrictedIC;
import org.enginehub.craftbook.util.RegexUtil;

/**
 * @author Silthus
 */
public class Pulser extends AbstractIC {

    // default values
    private int pulseLength;
    private int startDelay;
    private int pulseCount;
    private int pauseLength;

    private int taskId;
    private boolean running;

    public Pulser(Server server, ChangedSign block, ICFactory factory) {

        super(server, block, factory);
    }

    @Override
    public void load() {

        String line2 = getSign().getLine(2);
        String line3 = getSign().getLine(3);
        if (!(line2 == null) && !line2.isEmpty()) {
            String[] split = RegexUtil.COLON_PATTERN.split(line2, 2);
            pulseLength = Integer.parseInt(split[0]);
            if (split.length > 1) startDelay = Integer.parseInt(split[1]);
            else startDelay = 1;
        } else {
            pulseLength = 5;
            startDelay = 1;
        }
        if (!(line3 == null) && !line3.isEmpty()) {
            String[] split = RegexUtil.COLON_PATTERN.split(line3, 2);
            pulseCount = Math.max(1, Integer.parseInt(split[0]));
            if (split.length > 1) pauseLength = Math.max(1, Integer.parseInt(split[1]));
            else pauseLength = 5;
        } else {
            pulseCount = 1;
            pauseLength = 5;
        }
        getSign().setLine(2, pulseLength + ":" + startDelay);
        getSign().setLine(3, pulseCount + ":" + pauseLength);
        getSign().update(false);
    }

    @Override
    public String getTitle() {

        return "Pulser";
    }

    @Override
    public String getSignTitle() {

        return "PULSER";
    }

    @Override
    public final void trigger(ChipState chip) {

        if (getInput(chip)) {
            startThread(chip);
        }
    }

    private void startThread(ChipState chip) {

        if (running) return;
        // start a pulse task and run it every tick after the given delay
        // save the given task id
        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(CraftBookPlugin.inst(), new PulseTask(chip,
                pulseLength, pulseCount, pauseLength),
            startDelay, 1L);
        running = true;
    }

    private void stopThread() {

        Bukkit.getScheduler().cancelTask(taskId);
        running = false;
    }

    protected boolean getInput(ChipState chip) {

        //Concurrency issue? idk.
        return chip != null && chip.getInput(0);
    }

    protected void setOutput(ChipState chip, boolean on) {

        if (chip == null) return; //Concurrency issue? idk.
        chip.setOutput(0, on);
    }

    protected class PulseTask implements Runnable {

        private final ChipState chip;
        private final int pulseLength;
        private final int pulseCount;
        private final int pauseLength;

        // the amount of ticks this task was running for
        private int currentTick;
        // the current count of how many pulses were sent
        private int currentPulseCount;
        // the ticks of the current pulse. How long it is running
        private int currentPulse;
        private boolean on = false;

        public PulseTask(ChipState chip, int pulseLength, int pulseCount, int pauseLength) {

            if (pulseLength == 0) {
                pulseLength = 1;
            }
            if (pauseLength == 0) {
                pauseLength = 1;
            }

            this.chip = chip;
            this.pulseLength = pulseLength;
            this.pulseCount = pulseCount;
            this.pauseLength = pauseLength;
        }

        @Override
        public void run() {
            // first increase the current tick by one
            currentTick++;
            if (currentTick < 2) {
                startPulse();
                return;
            }
            if (on) {
                // stop the pulse if we reached the pulse length
                if (currentPulse % pulseLength == 0) {
                    stopPulse();
                } else {
                    increasePulse();
                }
            } else // start the next pulse if the pause is over
                if (currentTick % pauseLength == 0) {
                    startPulse();
                }
            // if all pulses were sent stop the thread
            if (!on && currentPulseCount % pulseCount == 0) {
                stopThread();
            }
        }

        private void startPulse() {

            setOutput(chip, true);
            currentPulse++;
            on = true;
        }

        private void stopPulse() {

            setOutput(chip, false);
            currentPulse = 0;
            currentPulseCount++;
            on = false;
        }

        private void increasePulse() {

            currentPulse++;
        }
    }

    public static class Factory extends AbstractICFactory implements RestrictedIC {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new Pulser(getServer(), sign, this);
        }

        @Override
        public void verify(ChangedSign sign) throws ICVerificationException {

            String line2 = sign.getLine(2);
            String line3 = sign.getLine(3);
            if (!(line2 == null) && !line2.isEmpty()) {
                try {
                    String[] split = RegexUtil.COLON_PATTERN.split(line2, 2);
                    Integer.parseInt(split[0]);
                    if (split.length > 1) Integer.parseInt(split[1]);
                } catch (Exception e) {
                    throw new ICVerificationException("You can only write numbers in line 3. See /icdocs for help");
                }
            }
            if (!(line3 == null) && !line3.isEmpty()) {
                try {
                    String[] split = RegexUtil.COLON_PATTERN.split(line3, 2);
                    Integer.parseInt(split[0]);
                    if (split.length > 1) Integer.parseInt(split[1]);
                } catch (Exception e) {
                    throw new ICVerificationException("You can only write numbers in line 4. See /icdocs for help");
                }
            }
        }

        @Override
        public String getShortDescription() {

            return "Fires a (choosable) pulse of high-signals with a choosable length of the signal "
                + "and the pause between the pulses when the input goes from low to high.";
        }

        @Override
        public String[] getPinDescription(ChipState state) {

            return new String[] {
                "Trigger IC",//Inputs
                "Pulse Output",//Outputs
            };
        }

        @Override
        public String[] getLineHelp() {

            return new String[] { "[pulselength[:startdelay]]", "[pulsecount[:pauselength in serverticks]]" };
        }
    }
}
