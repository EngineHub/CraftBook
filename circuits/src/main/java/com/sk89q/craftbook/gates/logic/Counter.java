package com.sk89q.craftbook.gates.logic;

import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import org.bukkit.Server;
import org.bukkit.block.Sign;

public class Counter extends AbstractIC {

	private int resetVal;
	private boolean inf;

    public Counter(Server server, Sign block) {
        super(server, block);
	    load();
    }

	private void load() {
		// Get IC configuration data from line 3 of sign
		String line2 = getSign().getLine(2);
		String[] config = line2.split(":");

		resetVal = 0;
		inf = false;
		try {
			resetVal = Integer.parseInt(config[0]);
			inf = config[1].equals("INF");
		} catch (NumberFormatException e) {
			resetVal = 5;
		} catch (ArrayIndexOutOfBoundsException e) {
			inf = false;
		} catch (Exception ignored) {
		}
		getSign().setLine(2, resetVal + (inf ? ":INF" : ""));
		getSign().update();
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
        // Get current counter value from line 4 of sign
        String line3 = getSign().getLine(3);
        int curVal;

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
                chip.setOutput(0, (curVal == resetVal));
                // If reset input triggered, reset counter value
            } else if (chip.isTriggered(1) && chip.get(1)) {
                curVal = 0;
	            chip.setOutput(0, false);
            }
        } catch (Exception ignored) {
        }

        // Update counter value stored on sign if it's changed
        if (curVal != oldVal) {
            getSign().setLine(3, curVal + "");
            getSign().update();
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
