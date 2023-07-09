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
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Server;
import org.enginehub.craftbook.ChangedSign;
import org.enginehub.craftbook.mechanics.ic.AbstractICFactory;
import org.enginehub.craftbook.mechanics.ic.AbstractSelfTriggeredIC;
import org.enginehub.craftbook.mechanics.ic.ChipState;
import org.enginehub.craftbook.mechanics.ic.IC;
import org.enginehub.craftbook.mechanics.ic.ICFactory;
import org.enginehub.craftbook.mechanics.ic.ICVerificationException;
import org.enginehub.craftbook.util.RegexUtil;

import java.util.Locale;

public class Monostable extends AbstractSelfTriggeredIC {

    // "Temp docs": nn:[HL] nn - time for pulse (1 = 2t) H: trigger on high L: trigger on low

    public Monostable(Server server, ChangedSign psign, ICFactory factory) {

        super(server, psign, factory);
    }

    @Override
    public String getTitle() {

        return "Monostable";
    }

    @Override
    public String getSignTitle() {

        return "MONOSTABLE";
    }

    @Override
    public boolean isAlwaysST() {

        return true;
    }

    @Override
    public void trigger(ChipState chip) {

        String setting = getLine(2).toUpperCase(Locale.ENGLISH);
        boolean triggered = chip.getInput(0);
        if (triggered && setting.contains("H") || !triggered && setting.contains("L")) {
            // Trigger condition!
            int colon = setting.indexOf(':');
            if (colon <= 0) return;

            chip.setOutput(0, true);
            getSign().setLine(3, Component.text(setting.substring(0, colon)));
            getSign().update(false);
        }
    }

    @Override
    public void think(ChipState chip) {

        int tick;

        try {
            tick = Integer.parseInt(getLine(3));
        } catch (NumberFormatException e) {
            tick = 0;
        }

        if (tick == 0) {
            chip.setOutput(0, false);
        } else {
            tick--;
        }

        getSign().setLine(3, Component.text(tick));
        getSign().update(false);
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {

            super(server);
        }

        @Override
        public IC create(ChangedSign sign) {

            return new Monostable(getServer(), sign, this);
        }

        @Override
        public String getShortDescription() {

            return "Outputs a pulse for a set amount of time on high.";
        }

        @Override
        public void verify(ChangedSign sign) throws ICVerificationException {

            int ticks;
            boolean hi;
            boolean lo;

            try {
                String set = PlainTextComponentSerializer.plainText().serialize(sign.getLine(2)).toUpperCase(Locale.ENGLISH);

                if (!set.contains(":")) throw new ICVerificationException("Invalid syntax");

                String[] settings = RegexUtil.COLON_PATTERN.split(set);

                if (settings.length != 2) throw new ICVerificationException("Invalid syntax");

                ticks = Integer.parseInt(settings[0]);

                hi = settings[1].contains("H");
                lo = settings[1].contains("L");
                if (!(hi || lo)) throw new ICVerificationException("Missing trigger levels");

            } catch (NumberFormatException e) {
                throw new ICVerificationException("Invalid number format");
            }

            ticks = Math.max(ticks, 2);
            ticks = Math.min(ticks, 6000);

            sign.setLine(2, Component.text(ticks + ":" + (hi ? "H" : "") + (lo ? "L" : "")));
            sign.setLine(3, Component.text("0"));
            sign.update(false);
        }
    }

}
