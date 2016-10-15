/*
 * CraftBook Copyright (C) 2010-2016 sk89q <http://www.sk89q.com>
 * CraftBook Copyright (C) 2011-2016 me4502 <http://www.me4502.com>
 * CraftBook Copyright (C) Contributors
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
package com.sk89q.craftbook.sponge.util;

public enum TernaryState {
    TRUE,FALSE,NONE;

    public static TernaryState getFromString(String s) {
        s = s.toLowerCase();

        if(s.equals("yes") || s.equals("true") || s.equals("y") || s.equals("t") || s.equals("1"))
            return TRUE;
        if(s.equals("no") || s.equals("false") || s.equals("n") || s.equals("f") || s.equals("0") || s.equals("not"))
            return FALSE;
        return NONE;
    }

    public boolean doesPass(boolean value) {
        switch (this) {
            case TRUE:
                return value;
            case FALSE:
                return !value;
            case NONE:
                return true;
            default:
                return false;
        }
    }
}