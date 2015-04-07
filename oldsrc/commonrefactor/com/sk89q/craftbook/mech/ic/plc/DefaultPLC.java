package com.sk89q.craftbook.mech.ic.plc;
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

import com.sk89q.craftbook.BlockType;
import com.sk89q.craftbook.access.ServerInterface;
import com.sk89q.craftbook.access.SignInterface;
import com.sk89q.craftbook.access.WorldInterface;
import com.sk89q.craftbook.util.SignText;
import com.sk89q.craftbook.util.Vector;

/**
 * A PLC that gets code from code block signs.
 *
 * @author Lymia
 */
public class DefaultPLC extends PlcBase {

    /**
     * Construct the instance.
     *
     * @param language
     */
    public DefaultPLC(PlcLang language) {

        super(language);
    }

    /**
     * Get the title of the IC.
     *
     * @return
     */
    public String getTitle() {

        return getLanguage().getName() + " PLC";
    }

    /**
     * Validates the IC's environment. The position of the sign is given.
     * Return a string in order to state an error message and deny
     * creation, otherwise return null to allow.
     *
     * @param sign
     *
     * @return
     */
    protected String validateEnviromentEx(ServerInterface i, WorldInterface w, Vector v, SignText t) {

        try {
            return getLanguage().validateEnvironment(w, v, t, getCode(w, v));
        } catch (PlcException e) {
            return e.toString();
        }
    }

    /**
     * Get the code.
     *
     * @param v
     */
    protected String getCode(WorldInterface w, Vector v) throws PlcException {

        StringBuilder b = new StringBuilder();
        int x = v.getBlockX();
        int z = v.getBlockZ();
        int x0 = x;
        int y0 = v.getBlockY();
        int z0 = z;
        for (int y = 0; y < 128; y++) {
            if (w.getId(x, y, z) == BlockType.WALL_SIGN) {
                if (((SignInterface) w.getBlockEntity(x, y, z)).getLine2()
                        .equalsIgnoreCase("[CODE BLOCK]"))
                    for (y--; y >= 0; y--) {
                        if (!(x == x0 && y == y0 && z == z0)
                                && w.getId(x, y, z) == BlockType.WALL_SIGN) {
                            SignInterface n = (SignInterface) w.getBlockEntity(x, y, z);
                            b.append(n.getLine1() + "\n");
                            b.append(n.getLine2() + "\n");
                            b.append(n.getLine3() + "\n");
                            b.append(n.getLine4() + "\n");
                        } else
                            return b.toString();
                    }
            }
        }
        throw new PlcException("code not found");
    }
}
