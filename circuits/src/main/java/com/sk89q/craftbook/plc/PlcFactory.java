// $Id$
/*
 * Copyright (C) 2012 Lymia Aluysia <lymiahugs@gmail.com>
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

package com.sk89q.craftbook.plc;

import java.util.Random;

import org.bukkit.Server;
import org.bukkit.block.Sign;
import org.bukkit.configuration.ConfigurationSection;

import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.ic.IC;
import com.sk89q.craftbook.ic.ICFactory;
import com.sk89q.craftbook.ic.ICVerificationException;

public class PlcFactory<StateT, CodeT, Lang extends PlcLanguage<StateT, CodeT>> implements ICFactory {
    private static Random RNG = new Random();

    private Lang lang;
    private boolean selfTriggered;
    private Server s;
    public PlcFactory(Server s, Lang lang, boolean selfTriggered) {
        this.s = s;
        this.lang = lang;
        this.selfTriggered = selfTriggered;
    }

    @Override
    public IC create(Sign sign) {
        PlcIC<StateT, CodeT, Lang> i = new PlcIC<StateT, CodeT, Lang>(s, sign, lang);
        return selfTriggered ? i.selfTriggered() : i;
    }

    @Override
    public void verify(Sign sign) throws ICVerificationException {
        new PlcIC<StateT, CodeT, Lang>(sign, lang); //Huge ugly hack!!
        sign.setLine(2, "id:"+Math.abs(RNG.nextInt()));
        if(!sign.getLine(3).isEmpty()) {
            String line = sign.getLine(3);
            if(!line.matches("[-_a-zA-Z0-9]+"))
                throw new ICVerificationException("illegal storage name");
        }
        sign.update();
    }

    @Override
    public void checkPlayer(Sign sign, LocalPlayer player) throws ICVerificationException {
        // Do nothing
    }

    public static <StateT, CodeT, Lang extends PlcLanguage<StateT, CodeT>>
    PlcFactory<StateT, CodeT, Lang> fromLang(Server s, Lang lang, boolean selfTriggered){
        return new PlcFactory<StateT, CodeT, Lang>(s, lang, selfTriggered);
    }

    @Override
    public String getDescription() {
        return "Programmable Logic Chip";
    }

    @Override
    public String[] getLineHelp() {
        return new String[] {
                "",
                ""
        };
    }

    @Override
    public void addConfiguration(ConfigurationSection section) {
    }
}
