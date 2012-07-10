package com.sk89q.craftbook.gates.logic;

import org.bukkit.Server;
import org.bukkit.block.Sign;

import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;

public class Counter extends AbstractIC{

    public Counter(Server server, Sign block) {
        super(server, block);
    }

    @Override
    public String getTitle() {
        return "Counter";
    }

    @Override
    public String getSignTitle() {
        return "COUNTER";
    }

    @Override
    public void trigger(ChipState chip) {
        // Get IC configuration data from line 3 of sign
        String[] config = getSign().getLine(2).split(":");

        int resetVal = 0;
        boolean inf = false;
        try {
            resetVal = Integer.parseInt(config[0]);
            inf = config[1].equalsIgnoreCase("INF");
        } catch (NumberFormatException e) {
            resetVal = 5;
        } catch (ArrayIndexOutOfBoundsException e) {
            inf = false;
        }
        // Get current counter value from line 4 of sign
        String line3 = getSign().getLine(3);
        int curVal = 0;

        try {
            curVal = Integer.parseInt(line3);
        } catch (Exception e) {
            curVal = 0;
        }

        int oldVal = curVal;
        try {
            // If clock input triggered
            if (chip.isTriggered(0) && chip.get(0)) {
                if (curVal == resetVal) { // If we've gotten to 0, reset if infinite mode
                    if (inf)
                        curVal = 0;
                } else { // increment counter
                    curVal++;
                }

                // Set output to high if we're at 0, otherwise low
                chip.set(3, (curVal == resetVal));
                // If reset input triggered, reset counter value
            } else if (chip.isTriggered(1) && chip.get(1)) {
                curVal = 0;
            }
        }
        catch(Exception e){
        }

        // Update counter value stored on sign if it's changed
        if (curVal != oldVal) {
            getSign().setLine(3, curVal + "");
        }
    }

    public static class Factory extends AbstractICFactory {
        public Factory(Server server) {
            super(server);
        }

        @Override
        public IC create(Sign sign) {
            return new Counter(getServer(), sign);
        }
    }
}
