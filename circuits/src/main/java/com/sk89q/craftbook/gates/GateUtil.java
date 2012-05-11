// $Id$
/*
 * CraftBook
 * Copyright (C) 2012 Lymia <lymiahugs@gmail.com>
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

package com.sk89q.craftbook.gates;

import org.bukkit.block.Sign;

public class GateUtil {
    private GateUtil() {}

    public static int getIntOrElse(Sign sign, int line, int def) {
        try {
            String l = sign.getLine(line);
            return l.isEmpty() ? def : Integer.parseInt(l);
        } catch(NumberFormatException e) {
            return def;
        }
    }
    public static int clamp(int i, int min, int max) {
        if(i<min) return min;
        if(i>max) return max;
        return i;
    }
}
