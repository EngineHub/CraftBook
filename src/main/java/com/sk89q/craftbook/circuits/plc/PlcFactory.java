// $Id$
/*
 * Copyright (C) 2012 Lymia Aluysia <lymiahugs@gmail.com>
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

package com.sk89q.craftbook.circuits.plc;

import org.bukkit.Server;

import com.sk89q.craftbook.ChangedSign;
import com.sk89q.craftbook.LocalPlayer;
import com.sk89q.craftbook.bukkit.CraftBookPlugin;
import com.sk89q.craftbook.circuits.ic.IC;
import com.sk89q.craftbook.circuits.ic.ICFactory;
import com.sk89q.craftbook.circuits.ic.ICVerificationException;
import com.sk89q.craftbook.util.RegexUtil;
import com.sk89q.util.yaml.YAMLProcessor;

public class PlcFactory<StateT, CodeT, Lang extends PlcLanguage<StateT, CodeT>> implements ICFactory {

    private Lang lang;
    private boolean selfTriggered;
    private Server s;

    public PlcFactory(Server s, Lang lang, boolean selfTriggered) {

        this.s = s;
        this.lang = lang;
        this.selfTriggered = selfTriggered;
    }

    @Override
    public IC create(ChangedSign sign) {

        PlcIC<StateT, CodeT, Lang> i = new PlcIC<StateT, CodeT, Lang>(s, sign, lang);
        return selfTriggered ? i.selfTriggered() : i;
    }

    @Override
    public void verify(ChangedSign sign) throws ICVerificationException {

        new PlcIC<StateT, CodeT, Lang>(sign, lang); // Huge ugly hack!!
        sign.setLine(2, "id:" + Math.abs(CraftBookPlugin.inst().getRandom().nextInt()));
        if (!sign.getLine(3).isEmpty()) {
            String line = sign.getLine(3);
            if (!RegexUtil.PLC_NAME_PATTERN.matcher(line).matches())
                throw new ICVerificationException("illegal storage name");
        }
        sign.update(false);
    }

    @Override
    public void checkPlayer(ChangedSign sign, LocalPlayer player) throws ICVerificationException {
        // Do nothing
    }

    public static <StateT, CodeT, Lang extends PlcLanguage<StateT, CodeT>> PlcFactory<StateT, CodeT,
    Lang> fromLang(Server s, Lang lang,

            boolean selfTriggered) {

        return new PlcFactory<StateT, CodeT, Lang>(s, lang, selfTriggered);
    }

    @Override
    public String getShortDescription() {

        return "Programmable Logic Chip";
    }

    @Override
    public String[] getLineHelp() {

        return new String[] {"", ""};
    }

    @Override
    public void addConfiguration(YAMLProcessor config, String path) {

    }

    @Override
    public boolean needsConfiguration() {

        return false;
    }

    @Override
    public String getLongDescription () {

        //TODO make this better.
        return "A Programmable Logic Chip is an IC that uses a language known as Perlstone to allow for custom logic to be written and ran.";
    }
}
