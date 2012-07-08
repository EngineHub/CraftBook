package com.sk89q.craftbook.gates.logic;

import org.bukkit.Server;
import org.bukkit.block.Sign;

import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;

/**
 * Counter counts down each time clock input toggles from low to high, it starts
 * from a predefined value to 0. Output is high when counter reaches 0. If in
 * 'infinite' mode, it will automatically reset the next time clock is toggled.
 * Otherwise, it only resets when the 'reset' input toggles from low to high.
 *
 * Configuration:
 * Line 3: ##:ONCE or ##:INF -- where ## is the counter reset value, and ONCE or INF
 *         specifies if the counter should repeat or not.
 * 
 * Inputs:
 *  1 - Clock
 *  2 - Reset
 *  3 - (unused)
 *
 * Output: HIGH when counter reaches 0, LOW otherwise
 *
 * @author davr
 */
public class DownCounter extends AbstractIC {
    public DownCounter(Server server, Sign sign) {
        super(server, sign);
    }

    @Override
    public String getTitle() {
        return "Down Counter";
    }

    @Override
    public String getSignTitle() {
        return "DOWN COUNTER";
    }

    @Override
    public void trigger(ChipState chip) {
        // Get IC configuration data from line 3 of sign
        String line2 = getSign().getLine(2);
        String[] config = line2.split(":");

        int resetVal = 0;
        boolean inf = false;
        try {
            resetVal = Integer.parseInt(config[0]);
            inf = config[1].equals("INF");
        } catch (NumberFormatException e) {
            resetVal = 5;
        } catch (ArrayIndexOutOfBoundsException e) {
            inf = false;
        }
        catch (Exception e) {
        }
        // Get current counter value from line 4 of sign
        String line3 = getSign().getLine(3);
        int curVal = 0;

        try {
            curVal = Integer.parseInt(line3);
        } catch (Exception e) {
            curVal = resetVal;
        }

        int oldVal = curVal;
        try {
            // If clock input triggered
            if (chip.isTriggered(0) && chip.get(0)) {
                if (curVal == 0) { // If we've gotten to 0, reset if infinite mode
                    if (inf)
                        curVal = resetVal;
                } else { // Decrement counter
                    curVal--;
                }

                // Set output to high if we're at 0, otherwise low
                chip.set(3, (curVal == 0));
                // If reset input triggered, reset counter value
            } else if (chip.isTriggered(1) && chip.get(1)) {
                curVal = resetVal;
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
            return new DownCounter(getServer(), sign);
        }
    }
}
