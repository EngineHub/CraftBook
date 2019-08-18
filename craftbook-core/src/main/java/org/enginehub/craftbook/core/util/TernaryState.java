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
package org.enginehub.craftbook.core.util;

public enum TernaryState {
    TRUE,FALSE,NONE;

    public static TernaryState getFromString(String s) {
        s = s.toLowerCase();

        if("yes".equals(s) || "true".equals(s) || "y".equals(s) || "t".equals(s) || "1".equals(s))
            return TRUE;
        if("no".equals(s) || "false".equals(s) || "n".equals(s) || "f".equals(s) || "0".equals(s) || "not".equals(s))
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