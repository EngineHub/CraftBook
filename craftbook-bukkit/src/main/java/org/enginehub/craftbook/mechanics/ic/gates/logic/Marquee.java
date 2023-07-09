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

import net.kyori.adventure.text.Component;
import org.bukkit.Server;
import org.enginehub.craftbook.ChangedSign;
import org.enginehub.craftbook.mechanics.ic.AbstractIC;
import org.enginehub.craftbook.mechanics.ic.AbstractICFactory;
import org.enginehub.craftbook.mechanics.ic.ChipState;
import org.enginehub.craftbook.mechanics.ic.IC;
import org.enginehub.craftbook.mechanics.ic.ICFactory;
import org.enginehub.craftbook.util.RegexUtil;

public class Marquee extends AbstractIC {

    public Marquee(Server server, ChangedSign sign, ICFactory factory) {

        super(server, sign, factory);
    }

    @Override
    public String getTitle() {

        return "Marquee";
    }

    @Override
    public String getSignTitle() {

        return "MARQUEE";
    }

    @Override
    public void trigger(ChipState chip) {

        if (!chip.getInput(0)) return;

        boolean reverse = false;
        int next = 0;
        try {
            String[] st = RegexUtil.RIGHT_BRACKET_PATTERN.split(getLine(1), 2);
            if (st.length > 1) {
                reverse = st[1].equalsIgnoreCase("r");
            }
            next = Integer.parseInt(getLine(2));
        } catch (Exception ignored) {
        }

        if (next == 0) {
            next = reverse ? 3 : 1;
        }
        for (short i = 0; i < chip.getOutputCount(); i++) {
            chip.setOutput(i, false); // Clear all pins
        }

        switch (next) {
            case 1:
                chip.setOutput(1, true);
                break;
            case 2:
                chip.setOutput(0, true);
                break;
            case 3:
                chip.setOutput(2, true);
                break;
        }

        if (reverse) {
            next--;
        } else {
            next++;
        }

        if (next == 0) {
            next = 3;
        } else if (next == 4) {
            next = 1;
        }

        // set the next output and update sign
        getSign().setLine(2, Component.text(next));
        getSign().update(false);

    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new Marquee(getServer(), sign, this);
        }

        @Override
        public String getShortDescription() {

            return "Sequentially sets all pins.";
        }

        @Override
        public String[] getPinDescription(ChipState state) {

            return new String[] {
                "Trigger IC",//Inputs
                "Output 1",//Outputs
                "Output 2",
                "Output 3",
            };
        }

        @Override
        public String[] getLineHelp() {

            return new String[] { "next pin to set", null };
        }
    }
}