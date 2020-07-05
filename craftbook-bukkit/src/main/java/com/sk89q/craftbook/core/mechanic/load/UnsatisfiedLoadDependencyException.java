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

package com.sk89q.craftbook.core.mechanic.load;

import com.sk89q.craftbook.core.mechanic.MechanicType;
import com.sk89q.craftbook.util.exceptions.MechanicInitializationException;

public class UnsatisfiedLoadDependencyException extends MechanicInitializationException {

    public UnsatisfiedLoadDependencyException(MechanicType<?> mechanicType, LoadDependency dependency) {
        super(mechanicType, "Dependency not satisfied: " + dependency.getDependencyId());
    }

}
