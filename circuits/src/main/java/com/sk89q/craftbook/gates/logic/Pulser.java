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
import com.sk89q.craftbook.ic.RestrictedIC;

/**
 * @author Silthus
 */
public class Pulser extends AbstractIC {

    // default values
    private int pulseLength = 5;
    private int startDelay = 1;
    private int pulseCount = 1;
    private int pauseLength = 5;

    private int taskId;
    private boolean running;

    public Pulser(Server server, ChangedSign block, ICFactory factory) {

        super(server, block, factory);
        load();
    }

    private void load() {

        try {
            ChangedSign sign = getSign();
            String line2 = sign.getLine(2);
            String line3 = sign.getLine(3);
            if (!(line2 == null) && !line2.equals("")) {
                try {
                    String[] split = line2.split(":");
                    pulseLength = Integer.parseInt(split[0]);
                    startDelay = Integer.parseInt(split[1]);
                } catch (Exception e) {
                    // defaults will be used
                }
            }
            if (!(line3 == null) && !line3.equals("")) {
                try {
                    String[] split = line3.split(":");
                    pulseCount = Integer.parseInt(split[0]);
                    pauseLength = Integer.parseInt(split[1]);
                } catch (Exception e) {
                    // defaults will be used
                }
            }
            sign.setLine(2, pulseLength + ":" + startDelay);
            sign.setLine(3, pulseCount + ":" + pulseLength);
            sign.update(false);
        } catch (Exception ignored) {
        }
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
        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(
                CircuitsPlugin.getInst(),
                new PulseTask(chip, pulseLength, pulseCount, pauseLength),
                startDelay, 1L);
        running = true;
    }

    private void stopThread() {

        Bukkit.getScheduler().cancelTask(taskId);
        running = false;
    }

    protected boolean getInput(ChipState chip) {

        return chip.getInput(0);
    }

    protected void setOutput(ChipState chip, boolean on) {

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

            this.chip = chip;
            this.pulseLength = pulseLength;
            this.pulseCount = pulseCount;
            this.pauseLength = pauseLength;

            if (pulseLength == 0) {
                pulseLength = 1;
            }
            if (pauseLength == 0) {
                pauseLength = 1;
            }
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
    }
}
