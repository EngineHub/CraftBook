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

package com.sk89q.craftbook.core.mechanic;

import com.sk89q.craftbook.CraftBookMechanic;

public class MechanicRegistration {

    private final String name;
    private final Class<? extends CraftBookMechanic> mechanicClass;
    private final MechanicCategory category;

    public MechanicRegistration(String name, Class<? extends CraftBookMechanic> mechanicClass, MechanicCategory category) {
        this.name = name;
        this.mechanicClass = mechanicClass;
        this.category = category;
    }

    public String getName() {
        return this.name;
    }

    public Class<? extends CraftBookMechanic> getMechanicClass() {
        return this.mechanicClass;
    }

    public MechanicCategory getCategory() {
        return this.category;
    }
}
