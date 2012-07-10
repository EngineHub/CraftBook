// $Id$
/*
 * Copyright (C) 2010, 2011 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.craftbook.gates.logic;

import org.bukkit.Server;
import org.bukkit.block.Sign;

import com.sk89q.craftbook.ic.AbstractIC;
import com.sk89q.craftbook.ic.AbstractICFactory;
import com.sk89q.craftbook.ic.ChipState;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.ICVerificationException;
import com.sk89q.craftbook.ic.SelfTriggeredIC;

public class Monostable extends AbstractIC implements SelfTriggeredIC{

    Sign sign;
    //"Temp docs": nn:[HL] nn - time for pulse (1 = 2t) H: trigger on high 	L: trigger on low

    public Monostable(Server server, Sign psign) {
        super(server, psign);
        sign = psign;
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
    public void trigger(ChipState chip) {

        String setting = sign.getLine(2).toUpperCase();
        if ( (chip.getInput(0) && setting.contains("H")) || (!chip.getInput(0) && setting.contains("L")) )
        {
            //Trigger condition!
            int index = setting.indexOf(":");
            if (index <= 0) return;

            chip.setOutput(0, true);
            sign.setLine(3, setting.substring(0, index) );

        }

    }

    @Override
    public void think(ChipState chip) {

        int tick;

        try
        {
            tick = Integer.parseInt(sign.getLine(3));
        } catch (NumberFormatException e) {
            tick = 0;
        }

        if (tick == 0)
            chip.setOutput(0, false);
        else
            tick --;

        sign.setLine(3, Integer.toString(tick));

    }

    @Override
    public boolean isActive() {
        return true;
    }

    public static class Factory extends AbstractICFactory {

        public Factory(Server server) {
            super(server);
        }

        @Override
        public IC create(Sign sign) {
            return new Monostable(getServer(), sign);
        }

        @Override
        public void verify(Sign sign) throws ICVerificationException
        {
            int ticks = -1;
            boolean hi = false;
            boolean lo = false;

            try
            {
                String set = sign.getLine(2).toUpperCase();

                if (set.indexOf(":") == -1)
                    throw new ICVerificationException("Invalid syntax");

                String[] settings = sign.getLine(2).split(":");

                if (settings.length != 2)
                    throw new ICVerificationException("Invalid syntax");

                ticks = Integer.parseInt(settings[0]);

                hi = settings[1].contains("H");
                lo = settings[1].contains("L");
                if ( !(hi || lo) )
                    throw new ICVerificationException("Missing trigger levels");


            } catch (NumberFormatException e) {
                throw new ICVerificationException("Invalid number format");
            } catch (ICVerificationException e) {
                throw e;
            }

            ticks = Math.max(ticks, 10);
            ticks = Math.min(ticks, 50);

            sign.setLine(2, Integer.toString(ticks) + ":" + (hi ? "H" : "") + (lo ? "L" : "")  );
            sign.setLine(3, "0");
        }
    }

}
