package com.sk89q.craftbook.mechanics.ic.gates.logic;

import org.bukkit.Server;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.mechanics.ic.AbstractICFactory;
import com.sk89q.craftbook.mechanics.ic.AbstractSelfTriggeredIC;
import com.sk89q.craftbook.mechanics.ic.ChipState;
import com.sk89q.craftbook.mechanics.ic.IC;
import com.sk89q.craftbook.mechanics.ic.ICFactory;
import com.sk89q.craftbook.util.RegexUtil;

/**
 * @author Me4502
 */
public class RangedOutput extends AbstractSelfTriggeredIC {

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
    public boolean isAlwaysST() {

        return true;
    }

    @Override
    public void think(ChipState chip) {

        chip.setOutput(0, shouldOutput(chip));
    }

    int min,max;

    int ticks = 0;
    int maxTicks = 0;
    boolean hasStarted = false;
    int amountDone = 0;
    int maxAmount = 0;

    @Override
    public void load() {
        String[] minmax = RegexUtil.MINUS_PATTERN.split(getSign().getLine(2));
        min = Integer.parseInt(minmax[0]);
        max = Integer.parseInt(minmax[1]);

        if (!getLine(3).isEmpty())
            maxTicks = Integer.parseInt(getSign().getLine(3));
        else
            maxTicks = 10;
    }

    protected boolean shouldOutput(ChipState chip) {

        if (chip.getInput(0)) {

            maxAmount = min + CraftBookPlugin.inst().getRandom().nextInt(max - min + 1);
            amountDone = 0;
            ticks = 0;

            hasStarted = true;
            return false;
        } else if (hasStarted)
            if (ticks >= maxTicks) {
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
    public void trigger(ChipState chip) {
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