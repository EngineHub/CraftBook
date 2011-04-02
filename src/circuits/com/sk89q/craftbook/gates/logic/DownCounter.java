package com.sk89q.craftbook.gates.logic;

import org.bukkit.*;
import org.bukkit.block.*;
import com.sk89q.craftbook.ic.*;
import com.sk89q.craftbook.util.*;

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
        return "Counter";
    }
    
    @Override
    public String getSignTitle() {
        return "DOWN COUNTER";
    }
    
    
    public String validateEnvironment(Sign sign) {
        String id = getSign().getLines()[2];
        if (id == null || !id.matches("^[0-9]+:(INF|ONCE)$"))
            return "Specify counter configuration on line 3.";
        
        getSign().getLines()[3] = "0";  //... do we really do this? and when exactly?!
        
        return null;
    }
    
    @Override
    public void trigger(ChipState chip) {
        // Get IC configuration data from line 3 of sign
        String line3 = getSign().getLines()[2];
        String[] config = line3.split(":");
        
        int resetVal = Integer.parseInt(config[0]);
        boolean inf = config[1].equals("INF");

        // Get current counter value from line 4 of sign
        String line4 = getSign().getLines()[4];
        int curVal = Integer.parseInt(line4);
        int oldVal = curVal;

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

        // Update counter value stored on sign if it's changed
        if (curVal != oldVal)
            getSign().getLines()[3] = (Integer.toString(curVal));
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
