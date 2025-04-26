/*
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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A size-limited hashmap that removes the oldest entry upon hitting the limit.
 *
 * @param <K> The key
 * @param <V> The value
 */
public final class HistoryHashMap<K, V> extends LinkedHashMap<K, V> {

    private static final long serialVersionUID = -3275917656900940011L;

    private final int maxEntries;

    public HistoryHashMap(int maxEntries) {
        super();
        this.maxEntries = maxEntries;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > maxEntries;
    }
}
