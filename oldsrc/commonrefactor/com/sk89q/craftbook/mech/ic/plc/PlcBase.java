// $Id$
/*
 * CraftBook
 * Copyright (C) 2010 Lymia <lymiahugs@gmail.com>
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

package com.sk89q.craftbook.mech.ic.plc;

import com.sk89q.craftbook.access.ServerInterface;
import com.sk89q.craftbook.access.WorldInterface;
import com.sk89q.craftbook.mech.ic.BaseIC;
import com.sk89q.craftbook.mech.ic.ChipState;
import com.sk89q.craftbook.mech.ic.Signal;
import com.sk89q.craftbook.util.SignText;
import com.sk89q.craftbook.util.Vector;

public abstract class PlcBase extends BaseIC {

    private PlcLang language;

    public PlcBase(PlcLang language) {

        this.language = language;
    }

    public void think(ChipState chip) {

        SignText t = chip.getText();

        String code;
        try {
            code = getCode(chip.getWorld(), chip.getPosition());
        } catch (PlcException e) {
            t.setLine2("§c" + t.getLine2());
            t.setLine3("!ERROR!");
            t.setLine4("code not found");
            return;
        }

        if (!t.getLine3().equals("HASH:" + Integer.toHexString(code.hashCode()))) {
            t.setLine2("§c" + t.getLine2());
            t.setLine3("!ERROR!");
            t.setLine4("code modified");
            return;
        }

        boolean[] output;
        try {
            output = language.tick(chip, code);
        } catch (PlcException e) {
            t.setLine2("§c" + t.getLine2());
            t.setLine3("!ERROR!");
            t.setLine4(e.getMessage());
            return;
        } catch (Throwable r) {
            t.setLine2("§c" + t.getLine2());
            t.setLine3("!ERROR!");
            t.setLine4(r.getClass().getSimpleName());
            return;
        }

        try {
            for (int i = 0; i < output.length; i++) {
                Signal out = chip.getOut(i + 1);
                if (out == null) break;
                out.set(output[i]);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            t.setLine2("§c" + t.getLine2());
            t.setLine3("!ERROR!");
            t.setLine4("too many outputs");
            return;
        }

        t.supressUpdate();
    }

    public String validateEnvironment(ServerInterface i, WorldInterface w, Vector v, SignText t) {

        if (!t.getLine3().isEmpty()) return "line 3 is not empty";

        String code;
        try {
            code = getCode(w, v);
        } catch (PlcException e) {
            return "Code block not found.";
        }

        t.setLine3("HASH:" + Integer.toHexString(code.hashCode()));

        return validateEnviromentEx(i, w, v, t);
    }

    protected abstract String validateEnviromentEx(ServerInterface i, WorldInterface world, Vector v, SignText t);

    protected abstract String getCode(WorldInterface world, Vector v) throws PlcException;

    protected PlcLang getLanguage() {

        return language;
    }
}
