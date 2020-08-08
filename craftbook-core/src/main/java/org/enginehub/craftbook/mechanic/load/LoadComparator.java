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

package com.sk89q.craftbook.mechanic.load;

import com.sk89q.craftbook.mechanic.MechanicType;

import java.util.Comparator;

public class LoadComparator implements Comparator<MechanicType<?>> {

    @Override
    public int compare(MechanicType<?> o1, MechanicType<?> o2) {
        // Compare dependencies first, these are more strict than load ordering
        for (LoadDependency dependency : o1.getDependencies()) {
            if (dependency instanceof MechanicDependency) {
                if (dependency.getDependencyId().equals(o2.getId())) {
                    // o1 depends on o2, so it must load beforehand.
                    return -1;
                }
            }
        }

        for (LoadDependency dependency : o2.getDependencies()) {
            if (dependency instanceof MechanicDependency) {
                if (dependency.getDependencyId().equals(o1.getId())) {
                    // o2 depends on o1, so it must load afterwards.
                    return 1;
                }
            }
        }

        // Load Priority is calculated after dependencies. Dependencies are always more important
        if (o1.getLoadPriority() == o2.getLoadPriority()) {
            return 0;
        }
        return o1.getLoadPriority().ordinal() < o2.getLoadPriority().ordinal() ? -1 : 1;
    }
}
