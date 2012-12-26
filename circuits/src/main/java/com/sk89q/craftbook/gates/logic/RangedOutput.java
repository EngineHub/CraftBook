package com.sk89q.craftbook.gates.logic;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.ic.*;
import org.bukkit.Server;

import java.util.regex.Pattern;

/**
 * @author Me4502
 */
public class RangedOutput extends AbstractIC implements SelfTriggeredIC {

    private static final Pattern MINUS_PATTERN = Pattern.compile("-", Pattern.LITERAL);
    int ticks = 0;
    int maxTicks = 0;
    boolean hasStarted = false;
    int amountDone = 0;
    int maxAmount = 0;

    public RangedOutput(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    @Override
    public String getTitle() {

        return "Ranged Output";
    }

    @Override
    public String getSignTitle() {

        return "RANGE OUTPUT";
    }

    @Override
    public void think(ChipState chip) {

        chip.setOutput(0, shouldOutput(chip));
    }

    protected boolean shouldOutput(ChipState chip) {

        if (chip.getInput(0)) {
            String[] minmax = MINUS_PATTERN.split(getSign().getLine(2));
            int min = Integer.parseInt(minmax[0]);
            int max = Integer.parseInt(minmax[1]);
            maxAmount = min + (int) (Math.random() * (max - min + 1));
            amountDone = 0;
            ticks = 0;

            if (getSign().getLine(3) != null || getSign().getLine(3).isEmpty()) {
                maxTicks = Integer.parseInt(getSign().getLine(3));
            } else {
                maxTicks = 10;
            }

            hasStarted = true;
            return false;
        } else if (hasStarted) if (ticks >= maxTicks) {
            amountDone++;
            if (amountDone >= maxAmount) {
                hasStarted = false;
                amountDone = 0;
                ticks = 0;
                maxAmount = 0;
            }
            return true;
        } else {
            ticks++;
            return false;
        }
        return false;
    }

    @Override
    public boolean isActive() {

        return true;
    }

    @Override
    public void trigger(ChipState chip) {
        // non-self triggered only
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new RangedOutput(getServer(), sign, this);
        }
    }
}
