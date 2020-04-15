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

package com.sk89q.craftbook;

import com.sk89q.craftbook.util.LoadPriority;

public abstract class AbstractCraftBookMechanic implements CraftBookMechanic, Comparable<LoadPriority> {

    @Override
    public boolean enable() {
        return true;
    }

    @Override
    public void disable() {

    }

    @Override
    public LoadPriority getLoadPriority() {

        return LoadPriority.STANDARD;
    }

    @Override
    public int compareTo(LoadPriority compare) {

        return compare.index < getLoadPriority().index ? -1 : 1;
    }
}