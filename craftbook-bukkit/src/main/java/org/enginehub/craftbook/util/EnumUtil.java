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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @author Silthus
 */
public final class EnumUtil {

    private EnumUtil() {
    }

    /**
     * Get the enum value of a string, null if it doesn't exist.
     */
    public static <T extends Enum<T>> T getEnumFromString(Class<T> c, String string) {

        if (c != null && string != null) {
            try {
                return Enum.valueOf(c, string.trim().toUpperCase(Locale.ENGLISH));
            } catch (IllegalArgumentException ignored) {
            }
        }
        return null;
    }

    public static String[] getStringArrayFromEnum(Class<? extends Enum<?>> c) {

        List<String> bits = new ArrayList<>();
        for(Enum<? extends Enum<?>> s : c.getEnumConstants())
            bits.add(s.name());
        return bits.toArray(new String[bits.size()]);
    }
}
