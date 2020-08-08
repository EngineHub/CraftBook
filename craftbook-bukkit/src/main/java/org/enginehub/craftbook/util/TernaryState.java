/*
 * CraftBook Copyright (C) me4502 <https://matthewmiller.dev/>
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

package org.enginehub.craftbook.util;

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

    public boolean doesPass(boolean bool) {
        switch (this) {
            case TRUE:
                return bool;
            case FALSE:
                return !bool;
            case NONE:
                return true;
            default:
                return false;
        }
    }
}