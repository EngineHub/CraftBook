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

import java.util.Locale;

/**
 * An object that represents three possible values. True, False, or Neither.
 */
public enum TernaryState {
    TRUE, FALSE, NONE;

    /**
     * Gets a TernaryState from the given string.
     *
     * <p>
     *     Note, if the value is not truthy or falsy, it is
     *     seen as {@link TernaryState#NONE}.
     * </p>
     *
     * @param s The string to parse
     * @return The parsed value
     */
    public static TernaryState parseTernaryState(String s) {
        switch (s.toLowerCase()) {
            case "yes":
            case "true":
            case "y":
            case "t":
            case "1":
                return TRUE;
            case "no":
            case "false":
            case "n":
            case "f":
            case "0":
                return FALSE;
        }

        return NONE;
    }

    /**
     * Gets whether the given test value passes this ternary state.
     *
     * @param test The test value
     * @return Whether it passes
     */
    public boolean doesPass(boolean test) {
        switch (this) {
            case TRUE:
                return test;
            case FALSE:
                return !test;
            case NONE:
                return true;
            default:
                return false;
        }
    }

    @Override
    public String toString() {
        return this.name().toLowerCase(Locale.ENGLISH);
    }
}
