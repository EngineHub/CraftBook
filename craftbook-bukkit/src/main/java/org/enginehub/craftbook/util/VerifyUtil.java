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

import java.util.Collection;
import java.util.Collections;

/**
 * A util file to verify many different things.
 */
public final class VerifyUtil {

    private VerifyUtil() {
    }

    /**
     * Verify that a radius is within the maximum.
     *
     * @param radius The radius to check
     * @param maxradius The maximum possible radius
     * @return The new fixed radius.
     */
    public static double verifyRadius(double radius, double maxradius) {

        return Math.max(0, Math.min(maxradius, radius));
    }

    public static <T> Collection<T> withoutNulls(Collection<T> list) {

        list.removeAll(Collections.singleton(null));

        return list;
    }
}