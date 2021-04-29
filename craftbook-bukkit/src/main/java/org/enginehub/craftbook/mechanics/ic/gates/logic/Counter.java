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

import org.bukkit.Server;
import org.enginehub.craftbook.ChangedSign;
import org.enginehub.craftbook.mechanics.ic.AbstractIC;
import org.enginehub.craftbook.mechanics.ic.AbstractICFactory;
import org.enginehub.craftbook.mechanics.ic.ChipState;
import org.enginehub.craftbook.mechanics.ic.IC;
import org.enginehub.craftbook.mechanics.ic.ICFactory;
import org.enginehub.craftbook.util.RegexUtil;

public class Counter extends AbstractIC {

    private int resetVal;
    private boolean inf;

    public Counter(Server server, ChangedSign block, ICFactory factory) {

        super(server, block, factory);
    }

    @Override
    public void load() {

        // Get IC configuration data from line 3 of sign
        String line2 = getSign().getLine(2);
        String[] config = RegexUtil.COLON_PATTERN.split(line2);

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
        getSign().update(false);
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
        // If clock input triggered
        if (chip.getInput(0)) {
            if (curVal == resetVal) { // If we've gotten to 0, reset if infinite mode
                if (inf) {
                    curVal = 0;
                }
            } else {
                curVal++;
            }

            // Set output to high if we're at 0, otherwise low
            chip.setOutput(0, curVal == resetVal);
            // If reset input triggered, reset counter value
        } else if (chip.getInput(1)) {
            curVal = 0;
            chip.setOutput(0, false);
        }

        // Update counter value stored on sign if it's changed
        if (curVal != oldVal) {
            getSign().setLine(3, String.valueOf(curVal));
            getSign().update(false);
        }
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new Counter(getServer(), sign, this);
        }

        @Override
        public String getShortDescription() {

            return "Increments on redstone signal, outputs high when reset.";
        }

        @Override
        public String[] getPinDescription(ChipState state) {

            return new String[] {
                "Trigger IC",//Inputs
                "Reset Counter",
                "Nothing",
                "High on Counter Complete"//Outputs
            };
        }

        @Override
        public String[] getLineHelp() {

            return new String[] { "reset ticks:(Optional)INF", "current ticks" };
        }
    }
}
